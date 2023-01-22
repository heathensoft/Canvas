#version 440

layout (location=0) in vec4 a_pos;
layout (location=1) in vec2 a_uv;

uniform mat4 u_combined;

out VS_OUT {
vec2 uv;
} vs_out;

void main() {

    vs_out.uv = a_uv;
    gl_Position = u_combined * a_pos;
}