#version 440

// can use this when drawinng back to front

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

//********************************************************************
// Common

#define COMMON_BINDING_POINT 0

struct Texture {
    mat4 textureToWorld;
    mat4 worldToTexture;
    vec4 bounds;
};

struct Camera {
    mat4 combined;
    mat4 combined_inv;
    vec3 position;
    float zoom;
};

layout (std140, binding = COMMON_BINDING_POINT) uniform CommonBlock {
    Camera camera;
    Texture tex;
    vec2 mouse_world;
    float tStep;
    float amplitude;
};

//********************************************************************
// Brush

#define BRUSH_TOOL_SAMPLER 0
#define BRUSH_TOOL_FREE_HAND 1
#define BRUSH_TOOL_LINE_DRAW 2
#define BRUSH_TOOL_DRAG 3

#define BRUSH_FUNCTION_NON 0
#define BRUSH_FUNCTION_SET 1
#define BRUSH_FUNCTION_ADD 2
#define BRUSH_FUNCTION_SUB 3
#define BRUSH_FUNCTION_MIX 4
#define BRUSH_FUNCTION_SMO 5
#define BRUSH_FUNCTION_SHA 6
#define BRUSH_NUM_FUNCTIONS 7

#define BRUSH_BINDING_POINT 2

struct Brush {
    vec3 contour_color;
    int texture_size;
    int function;
    int color_value;
    int tool;
    int shape;
};

layout (std140, binding = BRUSH_BINDING_POINT) uniform BrushBlock {
    Brush brush;
};

//********************************************************************

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

    if(brush_overlay_red == 1.0 && brush.tool != BRUSH_TOOL_SAMPLER) {

        float brush_color_red = fs_in.brush_color;
        int function = brush.function >= BRUSH_NUM_FUNCTIONS ||
        brush.function < 0 ? BRUSH_FUNCTION_NON : brush.function;

        if(function == BRUSH_FUNCTION_NON) {

            final_color = back_buffer_red;

        } else if(function == BRUSH_FUNCTION_SET) {

            final_color = brush_color_red;

        } else if(function == BRUSH_FUNCTION_ADD) {

            final_color = back_buffer_red + brush_color_red;

        } else if(function == BRUSH_FUNCTION_SUB) {

            final_color = back_buffer_red - brush_color_red;

        } else if(function == BRUSH_FUNCTION_MIX) {

            final_color = mix(brush_color_red, back_buffer_red, 0.5);

        } else{

            float[9] kernel = SMOOTHEN_KERNEL;

            ivec2 adjacent[9] = {
            ivec2(-1, 1), ivec2(0, 1), ivec2(1, 1),
            ivec2(-1, 0), ivec2(0, 0), ivec2(1, 0),
            ivec2(-1,-1), ivec2(0,-1), ivec2(1,-1) };

            if(function == BRUSH_FUNCTION_SMO) {

                kernel = SMOOTHEN_KERNEL;

            } else if(function == BRUSH_FUNCTION_SHA) {

                kernel = SHARPEN_KERNEL;
            }

            vec2 tex_size_inv = 1.0 / vec2(tex.bounds.zw - tex.bounds.xy);

            for(int i = 0; i < 9; i++) {
                vec2 uv = vec2(texel + adjacent[i]) * tex_size_inv;
                final_color += sampleBackBufferRed(uv) * kernel[i];
            }
        }

        final_color = clamp(final_color,0.0,1.0);

    } else {

        final_color = back_buffer_red;
    }

    f_color = vec4(final_color,final_color,final_color,1.0);

}
