#version 440

layout (location=0) in vec4 a_pos; // NDC
layout (location=0) in vec2 a_uv;

in VS_OUT {
    vec2 uv;
} vs_out;

void main() {

    vs_out.uv = a_uv;
    gl_Position = a_pos;

}
