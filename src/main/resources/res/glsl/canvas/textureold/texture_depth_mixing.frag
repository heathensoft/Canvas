#version 440

layout (location=0) out vec4 f_color;

uniform float u_detail_weight;
uniform sampler2D[2] u_sampler_array;

sampler2D detailSampler() {return u_sampler_array[0];}
sampler2D volumeSampler() {return u_sampler_array[1];}

vec4 fetchVolume(ivec2 texel) {return texelFetch(volumeSampler(), texel, 0);}
vec4 fetchDetail(ivec2 texel) {return texelFetch(detailSampler(), texel, 0);}

void main() {

    float weight = clamp(u_detail_weight,0.0,1.0);
    float detail_red = fetchDetail(ivec2(gl_FragCoord.xy)).r;
    float volume_red = fetchVolume(ivec2(gl_FragCoord.xy)).r;
    float mixed_red = mix(detail_red,volume_red,weight);
    f_color = vec4(mixed_red,0.0,0.0,1.0);

}


