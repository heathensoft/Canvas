#version 440

layout (location=0) out vec4 f_color;

in VS_OUT {
    vec2 uv;
} fs_in;

uniform sampler2D u_stroke_texture;
uniform vec4 u_contour_color;
uniform vec2 u_size_inv; // (1/width, 1/height)

const float fill_alpha = 0.1;
const vec2 adj[8] = vec2[8] (
vec2(-1, 1),vec2( 0, 1),vec2( 1, 1),
vec2(-1, 0),            vec2( 1, 0),
vec2(-1,-1),vec2( 0,-1),vec2( 1,-1)
);

void main() {

    vec4 color_out;
    vec2 uv = fs_in.uv;
    float red_channel = texture(u_stroke_texture,uv).r;

    if(red_channel == 1.0) {
        float accumulated = 0;
        color_out = u_contour_color;
        for(int i = 0; i < 8; i++) {
            vec2 sample_uv = (adj[i] * u_size_inv) + uv;
            accumulated += texture(u_stroke_texture,sample_uv).r;
        } if(accumulated < 8.0) {
            color_out.a *= fill_alpha;
        }
    }
    else {
        color_out = vec4(0.0,0.0,0.0,0.0);
    }

    f_color = color_out;
}
