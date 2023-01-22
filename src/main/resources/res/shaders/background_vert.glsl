#version 440

layout (location=0) in vec4 a_pos;

layout (std140, binding = 0) uniform UniformBlock {
    mat4 fullscreen_camera_combined;
    mat4 project_camera_combined;
    mat4 project_camera_combined_inv;
    vec4 project_texture_bounds;
};

out VS_OUT {
    vec2 uv;
    vec2 pos;
} vs_out;

const vec2 background_tex_size = vec2(2,2);
const float background_scale = 16;

void main() {

    gl_Position = a_pos;
    vs_out.pos = (project_camera_combined_inv * a_pos).xy;
    vs_out.uv = vs_out.pos * (1/background_tex_size) / background_scale;

}
