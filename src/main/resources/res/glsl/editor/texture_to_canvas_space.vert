#version 440

layout (location=0) in vec4 a_pos;
layout (location=1) in vec2 a_uv;

out VS_OUT {
    vec2 uv;
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

#define PROJECT_BINDING_POINT 2 // ************************************
// 36
struct Texture {
    mat4 textureToWorld;
    mat4 worldToTexture;
    vec4 bounds;
};
// 36
layout (std140, binding = PROJECT_BINDING_POINT) uniform ProjectBlock {
    Texture project_texture;
};
// ********************************************************************


vec2 ndc_to_uv(vec2 ndc);
vec2 uv_to_ndc(vec2 uv);

void main() {

    vec2 tex_pos_offset = vec2(project_texture.bounds.xy);
    vec2 tex_size = vec2(project_texture.bounds.zw) - tex_pos_offset;
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