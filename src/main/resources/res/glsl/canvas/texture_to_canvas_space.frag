#version 440

layout (location=0) out vec4 f_color_left;  // canvas side (current front buffer)
layout (location=1) out vec4 f_color_right; // preview side

in VS_OUT {
    vec2 uv;
} fs_in;

uniform sampler2D[3] u_sampler_array;

sampler2D canvasSampler()   { return u_sampler_array[0]; }
sampler2D previewSampler()  { return u_sampler_array[1]; }
sampler2D colorSampler()    { return u_sampler_array[2]; }

vec4 samplePreview(vec2 uv) { return texture(previewSampler(),uv); }
vec4 sampleCanvas(vec2 uv)  { return texture(canvasSampler(),uv); }
vec4 sampleColor(vec2 uv)   { return texture(colorSampler(),uv); }

void main() {

    vec4 preview_sample = samplePreview(fs_in.uv);
    vec4 canvas_sample = sampleCanvas(fs_in.uv);
    vec4 color_sample = sampleColor(fs_in.uv);

    float canvas_red = canvas_sample.r;
    float color_alpha = color_sample.a;

    vec4 canvas_color = vec4(
        canvas_red,
        canvas_red,
        canvas_red,
        color_alpha
    );

    vec4 preview_color = vec4(
            preview_sample.rgb,
            color_alpha
    );

    f_color_left = canvas_color;
    f_color_right = preview_color;
}