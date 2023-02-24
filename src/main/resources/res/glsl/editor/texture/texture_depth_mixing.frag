#version 440

layout (location=0) out float f_color;

uniform sampler2D[2] u_sampler_array;

#define EDITOR_BINDING_POINT 1 // *************************************
// 36
struct Camera {
    mat4 combined;
    mat4 combined_inv;
    vec3 position;
    float zoom;
};
// 44
layout (std140, binding = EDITOR_BINDING_POINT) uniform EditorBlock {
    Camera camera;
    vec2 mouse_world;
    float depth_amplitude;
    float detail_to_volume_ratio;
    vec2 std140_padding;
    float real_time;
    float preview_options; // cast to int
};

// ********************************************************************

sampler2D detailSampler() {return u_sampler_array[0];}
sampler2D volumeSampler() {return u_sampler_array[1];}

vec4 fetchVolume(ivec2 texel) {return texelFetch(volumeSampler(), texel, 0);}
vec4 fetchDetail(ivec2 texel) {return texelFetch(detailSampler(), texel, 0);}

void main() {

    float weight = clamp(detail_to_volume_ratio,0.0,1.0);
    float detail_red = fetchDetail(ivec2(gl_FragCoord.xy)).r;
    float volume_red = fetchVolume(ivec2(gl_FragCoord.xy)).r;
    float mixed_red = mix(volume_red,detail_red,weight);
    f_color = mixed_red;

}


