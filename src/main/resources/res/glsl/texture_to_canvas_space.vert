#version 440

layout (location=0) in vec4 a_pos;
layout (location=1) in vec2 a_uv;

layout (std140, binding = 0) uniform CommonBlock {
    mat4 screen_camera_combined;
    mat4 canvas_camera_combined;
    mat4 canvas_camera_combined_inv;
    vec4 canvas_texture_bounds;
    vec4 mouse_position_canvas;
};

out VS_OUT {
    vec2 uv;
} vs_out;

vec2 ndc_to_uv(vec2 ndc);
vec2 uv_to_ndc(vec2 uv);

void main() {

    vec2 tex_pos_offset = vec2(canvas_texture_bounds.xy);
    vec2 tex_size = vec2(canvas_texture_bounds.zw) - tex_pos_offset;
    vec2 tex_uv = a_uv;
    vec2 tex_pos = tex_uv * tex_size + tex_pos_offset;
    vec4 position = vec4(tex_pos,0.0,1.0);
    gl_Position = canvas_camera_combined * position;
    vs_out.uv = tex_uv;
}

vec2 ndc_to_uv(vec2 ndc) {
    float u = (ndc.x + 1.0) / 2.0;
    float v = (ndc.y + 1.0) / 2.0;
    return vec2(u,v);
}

vec2 uv_to_ndc(vec2 uv) {
    float x = uv.x * 2 - 1;
    float y = uv.y * 2 - 1;
    return vec2(x,y);
}