#version 440

layout (location=0) out float f_color;

uniform sampler2D[] u_sampler_array;

in GS_OUT {
    vec2 brush_uv;
} fs_in;

#define EDITOR_BINDING_POINT 1 // *************************************
// 36
struct Camera {
    mat4 combined;
    mat4 combined_inv;
    vec3 position;
    float zoom;
};
// 44
layout (std140, binding = EDITOR_BINDING_POINT) uniform EditorBlock {
    Camera camera;
    vec2 mouse_world;
    float depth_amplitude;
    float detail_to_volume_ratio;
    vec2 std140_padding;
    float real_time;
    float preview_options; // cast to int
};

#define BRUSH_BINDING_POINT 3 // **************************************

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

const vec2 UV_CENTER = vec2(0.5,0.5);

float sampleBrushRed(vec2 uv) { return texture(u_sampler_array[0],uv).r; }
float fetchColorAlpha(ivec2 texel) {return texelFetch(u_sampler_array[1],texel,0).a; }

void main() {
    /*
        USE:
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glBlendEquation(GL_MAX);

        If brush function = raise or lower we interpolate the color (smoothstep for round).
        This is not the actual color but the value to multiply the color with
        inside of the back to front shader.
    */
    float color = 0.0;

    if(fetchColorAlpha(ivec2(gl_FragCoord.xy)) > 0.1) {
        color = sampleBrushRed(fs_in.brush_uv); // 1.0 or 0.0
        if(color == 1.0 && brush.size > 2) {
            if(brush.function == BRUSH_FUNCTION_LOW || brush.function == BRUSH_FUNCTION_RAI) {
                float s = float(brush.size) / float(brush.texture_size.x);
                if(brush.shape == BRUSH_SHAPE_ROUND) {
                    float d = distance(UV_CENTER,fs_in.brush_uv);
                    float t = clamp(d / s / 2.0, 0.0, 1.0);
                    color *= (1 - (t * t * (3.0 - 2.0 * t)));
                } else if(brush.shape == BRUSH_SHAPE_SQUARE) {
                    float D = 0.5 * s;
                    float dx = distance(UV_CENTER.x,fs_in.brush_uv.x);
                    float dy = distance(UV_CENTER.y,fs_in.brush_uv.y);
                    color *= (1.0 - clamp(max(dx/D,dy/D),0.0,1.0));
                }
            }
        }
    }
    f_color = color;
}