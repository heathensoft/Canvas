#version 440

// can use this when drawinng back to front

layout (location=0) out vec4 f_color; // current channel front

in VS_OUT {
    float brush_color;
} fs_in;

uniform sampler2D[2] u_sampler_array;

sampler2D backBufferSampler() { return u_sampler_array[0]; }
sampler2D brushOverlaySampler() { return u_sampler_array[1]; }
float fetchBackBufferRed(ivec2 texel) { return texelFetch(backBufferSampler(),texel,0).r; }
float fetchBrushOverlayRed(ivec2 texel) { return texelFetch(brushOverlaySampler(),texel,0).r; }

//********************************************************************
// Common

#define COMMON_BINDING_POINT 0

struct Camera {
    mat4 combined;
    mat4 combined_inv;
    vec3 position;
    float zoom;
};

layout (std140, binding = COMMON_BINDING_POINT) uniform CommonBlock {
    Camera camera;
    vec4 texture_bounds;
    vec2 mouse_world;
    float tStep;
    float amplitude;
};

//********************************************************************
// Brush

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
    vec4 contour_color;
    int texture_size;
    int function;
    int color_value;
    int std140_padding;
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
-1.000,  5.0000,-1.000,
-0.250, -1.000, -0.250
};

void main() {

    ivec2 texel = ivec2(gl_FragCoord.xy);
    float brush_overlay_red = fetchBrushOverlayRed(texel);
    float back_buffer_red = fetchBackBufferRed(texel);
    float final_color = 0.0;

    if(brush_overlay_red == 1.0) {

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

            float[9] kernel;

            ivec2 adjacent[9] = {
            ivec2(-1, 1), ivec2(0, 1), ivec2(1, 1),
            ivec2(-1, 0), ivec2(0, 0), ivec2(1, 0),
            ivec2(-1,-1), ivec2(0,-1), ivec2(1,-1) };

            if(function == BRUSH_FUNCTION_SMO) {

                kernel = SMOOTHEN_KERNEL;

            } else if(function == BRUSH_FUNCTION_SHA) {

                kernel = SHARPEN_KERNEL;
            }

            for(int i = 0; i < 9; i++) {

                final_color += fetchBackBufferRed(texel + adjacent[i]) * kernel[i];
            }
        }

        final_color = clamp(final_color,0.0,1.0);

    } else {

        final_color = back_buffer_red;
    }

    f_color = vec4(final_color,final_color,final_color,1.0);

}
