#version 440

layout (location=0) in vec2 a_uv;

out VS_OUT {
    vec2 uv;
} vs_out;

vec2 uv_to_ndc(vec2 uv) {
    float x = uv.x * 2 - 1;
    float y = uv.y * 2 - 1;
    return vec2(x,y);
}

void main() {

    vs_out.uv = a_uv;
    gl_Position = vec4(uv_to_ndc(a_uv),0.0,1.0);

}