#version 440

layout (location=0) in vec4 a_pos;
layout (location=1) in vec2 a_uv;

out VS_OUT {
    float vertical_split;
} vs_out;

uniform float u_dividing_line;

void main() {

    vs_out.vertical_split = a_uv.x * u_dividing_line;
    gl_Position = a_pos;
}