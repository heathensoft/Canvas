#version 440

layout (location=0) out vec4 f_color;

uniform float u_detail_weight;
uniform sampler2D[2] u_sampler_array;

sampler2D detailSampler() {return u_sampler_array[0];}
sampler2D volumeSampler() {return u_sampler_array[1];}

vec4 sampleVolume(ivec2 texel) {return texelFetch(volumeSampler(), texel, 0);}
vec4 sampleDetail(ivec2 texel) {return texelFetch(detailSampler(), texel, 0);}

void main() {

    u_detail_weight = clamp(u_detail_weight,0.0,1.0);
    float detail_red = sampleDetail(ivec2(gl_FragCoord)).r;
    float volume_red = sampleVolume(ivec2(gl_FragCoord)).r;
    float mixed_red = mix(detail_red,volume_red,u_detail_weight);
    f_color = vec4(mixed_red,0.0,0.0,1.0);

}


