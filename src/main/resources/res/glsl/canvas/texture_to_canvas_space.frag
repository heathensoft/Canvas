#version 440

layout (location=0) out vec4 f_color_left;  // canvas side (current front buffer)
layout (location=1) out vec4 f_color_right; // preview side
// layout (location=2) out vec4 f_contour; // contour

in VS_OUT {
    vec2 uv;
} fs_in;

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
#define BRUSH_TOOL_RECTANGLE 3

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

uniform sampler2D[4] u_sampler_array;

sampler2D canvasSampler()   { return u_sampler_array[0]; }
sampler2D previewSampler()  { return u_sampler_array[1]; }
sampler2D colorSampler()    { return u_sampler_array[2]; }
sampler2D brushSampler()    { return u_sampler_array[3]; }

vec4 samplePreview(vec2 uv) { return texture(previewSampler(),uv); }
vec4 sampleCanvas(vec2 uv)  { return texture(canvasSampler(),uv); }
vec4 sampleColor(vec2 uv)   { return texture(colorSampler(),uv); }
vec4 sampleBrushOverlay(vec2 uv)   { return texture(brushSampler(), uv); }

const vec4 CONTOUR_COLOR = vec4(0.0,1.0,0.0,1.0);
const vec2 ADJACENT_ARRAY[8] = {
vec2(-1.0, 1.0), vec2(0.0, 1.0), vec2(1.0, 1.0),
vec2(-1.0, 0.0),                 vec2(1.0, 0.0),
vec2(-1.0,-1.0), vec2(0.0,-1.0), vec2(1.0,-1.0)
};

void main() {


    vec4 preview_sample = samplePreview(fs_in.uv);
    vec4 canvas_sample = sampleCanvas(fs_in.uv);
    vec4 color_sample = sampleColor(fs_in.uv);

    float canvas_red = canvas_sample.r;
    float color_alpha = color_sample.a;

    vec4 preview_color = vec4(
        preview_sample.rgb,
        color_alpha
    );

    vec4 canvas_color = vec4(
        canvas_red,
        canvas_red,
        canvas_red,
        color_alpha
    );

    // *******************************************
    // Brush contour

    // if tool is sample. then output contour to both

    /*
    vec4 brush_sample = sampleBrushOverlay(fs_in.uv);
    float brush_red = brush_sample.r;

    if(brush_red == 1.0) {
        vec2 tex_size = vec2(tex.bounds.zw - tex.bounds.xy);
        vec2 tex_size_inv = 1.0 / tex_size;
        float accumulated = 0;
        for(int i = 0; i < 8; i++) {
            vec2 sample_uv = fs_in.uv + (ADJACENT_ARRAY[i] * tex_size_inv);
            accumulated += sampleBrushOverlay(sample_uv).r;
        }
        if(accumulated < 8.0) {
            preview_color = CONTOUR_COLOR;
            if(brush.tool == BRUSH_TOOL_SAMPLER) {
                preview_color = CONTOUR_COLOR;
            }
        }
    }
    */

    // *******************************************


    f_color_left = canvas_color;
    f_color_right = preview_color;
}