#version 440

layout (location=0) in vec4 a_pos; // NDC
layout (location=1) in vec2 a_uv;

out VS_OUT {
    vec2 uv;
} vs_out;

void main() {

    gl_Position = a_pos;
    uv = a_uv;
}
