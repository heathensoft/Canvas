#version 440

layout (location=0) out vec4 f_color;

uniform sampler2D u_sampler_2D;

in vec2 uv;
in vec4 color;

float sampleContourRed(vec2 uv) {
    return texture(u_sampler_2D,uv).r;
}

void main() {

    f_color = color;
    if(sampleContourRed(uv) != 1.0) discard;
}