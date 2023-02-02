#version 440

layout (location=0) out vec4 f_color;

uniform sampler2D[2] u_sampler_array;

in GS_OUT {
    vec2 brush_uv;
} fs_in;

//********************************************************************
// Common

#define COMMON_BINDING_POINT 0

struct Texture {
    mat4 textureToWorld;
    mat4 worldToTexture;
    vec4 bounds;
};

struct Camera {
    mat4 combined;
    mat4 combined_inv;
    vec3 position;
    float zoom;
};

layout (std140, binding = COMMON_BINDING_POINT) uniform CommonBlock {
    Camera camera;
    Texture tex;
    vec2 mouse_world;
    float tStep;
    float amplitude;
};

//********************************************************************

sampler2D brushSampler() { return u_sampler_array[0]; }
sampler2D colorSampler() { return u_sampler_array[1]; }

float fetchColorAlpha(ivec2 texel) { return texelFetch(colorSampler(),texel,0).a; }
float sampleBrushTexture(vec2 uv) { return texture(brushSampler(),uv).r; }

void main() {

    f_color = vec4(0.0,0.0,0.0,1.0);
    if(sampleBrushTexture(fs_in.brush_uv) == 1.0) {
        if(fetchColorAlpha(ivec2(gl_FragCoord.xy)) > 0.1) {
            f_color = vec4(1.0,0.0,0.0,1.0);
        } else discard;
    } else discard;

}