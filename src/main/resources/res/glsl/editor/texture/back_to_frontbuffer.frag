#version 440

layout (location=0) out vec4 f_color; // current channel front

uniform sampler2D[2] u_sampler_array;

in VS_OUT {
    float brush_color;
} fs_in;

sampler2D backBufferSampler() { return u_sampler_array[0]; }
sampler2D brushOverlaySampler() { return u_sampler_array[1]; }
float fetchBackBufferRed(ivec2 texel) { return texelFetch(backBufferSampler(),texel,0).r; }
float fetchBrushOverlayRed(ivec2 texel) { return texelFetch(brushOverlaySampler(),texel,0).r; }
float sampleBackBufferRed(vec2 uv) { return texture(backBufferSampler(), uv).r; }

#define PROJECT_BINDING_POINT 2 // ************************************
// 36
struct Texture {
    mat4 textureToWorld;
    mat4 worldToTexture;
    vec4 bounds;
};
// 36
layout (std140, binding = PROJECT_BINDING_POINT) uniform ProjectBlock {
    Texture project_texture;
};

#define BRUSH_BINDING_POINT 3 // **************************************

#define BRUSH_TOOL_SAMPLER 0
#define BRUSH_TOOL_FREE_HAND 1
#define BRUSH_TOOL_LINE_DRAW 2
#define BRUSH_TOOL_DRAG_AREA 3
#define BRUSH_NUM_TOOLS 4
#define BRUSH_SHAPE_ROUND 0
#define BRUSH_SHAPE_SQUARE 1
#define BRUSH_NUM_SHAPES 2
#define BRUSH_FUNCTION_NON 0
#define BRUSH_FUNCTION_SET 1
#define BRUSH_FUNCTION_ADD 2
#define BRUSH_FUNCTION_SUB 3
#define BRUSH_FUNCTION_MIX 4
#define BRUSH_FUNCTION_SMO 5
#define BRUSH_FUNCTION_SHA 6
#define BRUSH_FUNCTION_RAI 7
#define BRUSH_FUNCTION_LOW 8
#define BRUSH_NUM_FUNCTIONS 9
// 8
struct Brush {
    ivec2 texture_size;
    int function;
    int color;
    int tool;
    int shape;
    int size;
    int std140_padding;
};
// 8
layout (std140, binding = BRUSH_BINDING_POINT) uniform BrushBlock {
    Brush brush;
};
//*********************************************************************

const float[9] SMOOTHEN_KERNEL = {
0.0625, 0.1250, 0.0625,
0.1250 ,0.2500 ,0.1250,
0.0625, 0.1250, 0.0625
};

const float[9] SHARPEN_KERNEL = {
-0.250, -1.000, -0.250,
-1.000,  6.0000,-1.000,
-0.250, -1.000, -0.250
};

void main() {

    ivec2 texel = ivec2(gl_FragCoord.xy);
    float brush_overlay_red = fetchBrushOverlayRed(texel);
    float back_buffer_red = fetchBackBufferRed(texel);
    float final_color = 0.0;

    if(brush_overlay_red > 0.0 && brush.tool != BRUSH_TOOL_SAMPLER) {

        float brush_color_red = fs_in.brush_color;
        int function = brush.function >= BRUSH_NUM_FUNCTIONS ||
        brush.function < 0 ? BRUSH_FUNCTION_NON : brush.function;
        if(function == BRUSH_FUNCTION_NON) final_color = back_buffer_red;
        else if(function == BRUSH_FUNCTION_SET) final_color = brush_color_red;
        else if(function == BRUSH_FUNCTION_ADD) final_color = back_buffer_red + brush_color_red;
        else if(function == BRUSH_FUNCTION_SUB) final_color = back_buffer_red - brush_color_red;
        else if(function == BRUSH_FUNCTION_MIX) final_color = mix(brush_color_red, back_buffer_red, 0.5);
        else if(function == BRUSH_FUNCTION_SMO || function == BRUSH_FUNCTION_SHA) {
            float[9] kernel = function == BRUSH_FUNCTION_SMO ? SMOOTHEN_KERNEL : SHARPEN_KERNEL;
            ivec2 adjacent[9] = {
                ivec2(-1, 1), ivec2(0, 1), ivec2(1, 1),
                ivec2(-1, 0), ivec2(0, 0), ivec2(1, 0),
                ivec2(-1,-1), ivec2(0,-1), ivec2(1,-1) };
            vec2 tex_size_inv = 1.0 / vec2(project_texture.bounds.zw - project_texture.bounds.xy);
            for(int i = 0; i < 9; i++) {
                vec2 uv = vec2(texel + adjacent[i]) * tex_size_inv;
                final_color += sampleBackBufferRed(uv) * kernel[i];
            }
        } else if(function == BRUSH_FUNCTION_RAI) {
            final_color = back_buffer_red + brush_overlay_red * brush_color_red;
            final_color = min(final_color,brush_color_red);
        } else if(function == BRUSH_FUNCTION_LOW) {
            final_color = back_buffer_red - brush_overlay_red * brush_color_red;

        }
        final_color = clamp(final_color,0.0,1.0);

    } else final_color = back_buffer_red;

    f_color = vec4(final_color,final_color,final_color,1.0);

}
