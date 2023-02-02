#version 440

layout (location=0) in vec4 a_pos;
layout (location=1) in vec2 a_uv;

out VS_OUT {
    vec2 uv;
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


vec2 ndc_to_uv(vec2 ndc);
vec2 uv_to_ndc(vec2 uv);

void main() {

    vec2 tex_pos_offset = vec2(tex.bounds.xy);
    vec2 tex_size = vec2(tex.bounds.zw) - tex_pos_offset;
    vec2 tex_uv = a_uv;
    vec2 tex_pos = tex_uv * tex_size + tex_pos_offset;
    vec4 position = vec4(tex_pos,0.0,1.0);
    gl_Position = camera.combined * position;
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