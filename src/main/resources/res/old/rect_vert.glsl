#version 440

layout (location=0) in vec4 a_pos;

uniform mat4 u_combined;

void main() {

    gl_Position = u_combined * a_pos;

}