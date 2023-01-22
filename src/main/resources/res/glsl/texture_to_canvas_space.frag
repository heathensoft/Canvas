#version 440

layout (location=0) out vec4 f_color_left;  // canvas side (current front buffer)
layout (location=1) out vec4 f_color_right; // preview side

in VS_OUT {
    vec2 uv;
} fs_in;

uniform sampler2D[2] u_sampler_array;

sampler2D canvasSampler() {
    return u_sampler_array[0];
}

sampler2D previewSampler() {
    return u_sampler_array[1];
}

vec4 samplePreview(vec2 uv) {
    return texture(previewSampler(),uv);
}

vec4 sampleCanvas(vec2 uv) {
    return texture(canvasSampler(),uv);
}

void main() {

    vec4 preview_sample = samplePreview(fs_in.uv);
    vec4 canvas_sample = sampleCanvas(fs_in.uv);

    float canvas_red = canvas_sample.r;
    float preview_alpha = preview_sample.a;

    vec4 canvas_color = vec4(
        canvas_red,
        canvas_red,
        canvas_red,
        preview_alpha
    );

    f_color_left = canvas_color;
    f_color_right = preview_sample;
}