#version 440

layout (location=0) out vec4 f_color;

uniform sampler2D[2] u_sampler_array;

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


const vec3 FLAT_BLUE = vec3(0.5,0.5,1.0);


float normalDepth(float d, float amp) {return (2.0 * d - 1.0) * amp;}

sampler2D depthSampler() {return u_sampler_array[0];}
sampler2D colorSampler() {return u_sampler_array[1];}

vec4 fetchColor(ivec2 texel) {return texelFetch(colorSampler(), texel, 0);}
vec4 fetchDepth(ivec2 texel) {return texelFetch(depthSampler(), texel, 0);}

ivec2 ce() {return (ivec2(gl_FragCoord.xy));}
ivec2 up() {return (ivec2(gl_FragCoord.xy) + ivec2( 0, 1));}
ivec2 ri() {return (ivec2(gl_FragCoord.xy) + ivec2( 1, 0));}
ivec2 dw() {return (ivec2(gl_FragCoord.xy) + ivec2( 0,-1));}
ivec2 le() {return (ivec2(gl_FragCoord.xy) + ivec2(-1, 0));}


void main() {

    vec3 color_out = vec3(FLAT_BLUE);

    ivec2 texel = ce();

    if(fetchColor(texel).a > 0.0) {
        float xlim = tex.bounds.z - tex.bounds.x - 1;
        float ylim = tex.bounds.w - tex.bounds.y - 1;
        float d = normalDepth(fetchDepth(texel).r, amplitude);
        float hr = texel.x < xlim ? normalDepth(fetchDepth(ri()).r, amplitude) : d;
        float hu = texel.y < ylim ? normalDepth(fetchDepth(up()).r, amplitude) : d;
        float hl = texel.x > 0    ? normalDepth(fetchDepth(le()).r, amplitude) : d;
        float hd = texel.y > 0    ? normalDepth(fetchDepth(dw()).r, amplitude) : d;
        color_out = normalize(vec3(hl - hr, hd - hu, 2.0)) * 0.5 + 0.5;
    }

    f_color = vec4(color_out,1.0);

}