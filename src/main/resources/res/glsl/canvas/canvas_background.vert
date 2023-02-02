#version 440

layout (location=0) in vec4 a_pos;

out VS_OUT {
    vec2 uv;
    vec2 pos;
} vs_out;

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

const float BG_SCALE = 16;
const vec2 BG_TEX_SIZE = vec2(2, 2);

void main() {

    gl_Position = a_pos;
    vs_out.pos = vec2((camera.combined_inv * a_pos).xy);
    vs_out.uv = vs_out.pos * (1/ BG_TEX_SIZE) / BG_SCALE;

}
