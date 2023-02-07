#version 440

layout (location=0) in vec4 a_pos;

out VS_OUT {
    vec2 uv;
    vec2 pos;
} vs_out;

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

// ********************************************************************

const float BG_SCALE = 16;
const vec2 BG_TEX_SIZE = vec2(2, 2);

void main() {

    gl_Position = a_pos;
    vs_out.pos = vec2((camera.combined_inv * a_pos).xy);
    vs_out.uv = vs_out.pos * (1/ BG_TEX_SIZE) / BG_SCALE;

}
