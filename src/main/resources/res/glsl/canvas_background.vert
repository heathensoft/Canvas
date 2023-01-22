#version 440

layout (location=0) in vec4 a_pos;

layout (std140, binding = 0) uniform CommonBlock {
    mat4 screen_camera_combined;
    mat4 canvas_camera_combined;
    mat4 canvas_camera_combined_inv;
    vec4 canvas_texture_bounds;
    vec4 mouse_position_canvas;
};

out VS_OUT {
    vec2 uv;
    vec2 pos;
} vs_out;

const float BG_SCALE = 16;
const vec2 BG_TEX_SIZE = vec2(2, 2);

void main() {

    gl_Position = a_pos;
    vs_out.pos = vec2((canvas_camera_combined_inv * a_pos).xy);
    vs_out.uv = vs_out.pos * (1/ BG_TEX_SIZE) / BG_SCALE;

}
