#version 440

layout (location=0) out vec4 f_color;

in VS_OUT {
    vec2 uv;
} fs_in;

uniform sampler2D u_source_texture;

void main() {

    // for R8 this would be vec4(red,0,0,1)
    f_color = texture(u_source_texture,fs_in.uv);

}
