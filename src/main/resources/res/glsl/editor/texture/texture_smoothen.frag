#version 440

layout (location=0) out vec4 f_color;

uniform sampler2D u_sampler_2d;

in vec2 uv;

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

void main() {

    float color = 0.0;

    const float[9] kernel = {
    0.0625, 0.1250, 0.0625,
    0.1250 ,0.2500 ,0.1250,
    0.0625, 0.1250, 0.0625
    };

    const vec2 adj[9] = {
    vec2(-1, 1), vec2(0, 1), vec2(1, 1),
    vec2(-1, 0), vec2(0, 0), vec2(1, 0),
    vec2(-1,-1), vec2(0,-1), vec2(1,-1) };


    vec2 tex_size_inv = 1.0 / vec2(project_texture.bounds.zw - project_texture.bounds.xy);

    for(int i = 0; i < 9; i++) {
        vec2 sample_uv = uv + adj[i] * tex_size_inv;
        color += texture(u_sampler_2d, sample_uv).r * kernel[i];
    }

    f_color = vec4(color,color,color,1.0);
}