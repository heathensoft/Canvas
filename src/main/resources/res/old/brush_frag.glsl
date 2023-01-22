#version 440

// blending disabled
layout (location=0) out vec4 f_color;

in GS_OUT {
    vec2 brush_uv;
} fs_in;

uniform sampler2D u_brush_texture;
uniform sampler2D u_source_rgba;
uniform vec2 u_framebuffer_size;

void main() {

    float red = texture(u_brush_texture,fs_in.brush_uv).r;
    if(red == 1.0) {
        vec2 uv = vec2(gl_FragCoord.xy) / u_framebuffer_size;
        float alpha = texture(u_source_rgba,uv).a;
        if(alpha > 0.1) {
            f_color = vec4(1.0,0.0,0.0,1.0);
        }
    }
    discard;

}