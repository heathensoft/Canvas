#version 440

// blending disbled
layout (location=0) out vec4 f_color;

uniform vec2 u_framebuffer_size;
uniform sampler2D u_source_rgba;

void main() {

    vec2 uv = vec2(gl_FragCoord.xy) / u_framebuffer_size;
    float alpha = texture(u_source_rgba,uv).a;
    if(alpha > 0.1) {
        f_color = vec4(1.0,0.0,0.0,1.0);
    }
    discard;

}
