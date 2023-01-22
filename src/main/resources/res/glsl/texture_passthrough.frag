#version 440

layout (location=0) out vec4 f_color;

uniform sampler2D u_sampler;


void main() {

    f_color = texelFetch(u_sampler,ivec2(gl_FragCoord.xy),0);
}