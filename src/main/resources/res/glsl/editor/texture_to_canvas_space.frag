#version 440

layout (location=0) out vec4 f_color_left;  // canvas side (current front buffer)
layout (location=1) out vec4 f_color_right; // preview side
layout (location=2) out vec4 f_color_contour; // contour (R8)

in VS_OUT {
    vec2 uv;
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
//*********************************************************************

uniform sampler2D[4] u_sampler_array;

sampler2D canvasSampler()   { return u_sampler_array[0]; }
sampler2D previewSampler()  { return u_sampler_array[1]; }
sampler2D colorSampler()    { return u_sampler_array[2]; }
sampler2D brushSampler()    { return u_sampler_array[3]; }

vec4 samplePreview(vec2 uv) { return texture(previewSampler(),uv); }
vec4 sampleCanvas(vec2 uv)  { return texture(canvasSampler(),uv); }
vec4 sampleColor(vec2 uv)   { return texture(colorSampler(),uv); }
vec4 sampleBrushOverlay(vec2 uv)   { return texture(brushSampler(), uv); }

void main() {

    vec4 brush_sample = sampleBrushOverlay(fs_in.uv);
    vec4 preview_sample = samplePreview(fs_in.uv);
    vec4 canvas_sample = sampleCanvas(fs_in.uv);
    vec4 color_sample = sampleColor(fs_in.uv);

    float brush_red = brush_sample.r;
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

    vec4 contour_color = vec4(
            brush_red,
            brush_red,
            brush_red,
            1.0
    );

    f_color_left = canvas_color;
    f_color_right = preview_color;
    f_color_contour = contour_color;
}