#version 440

layout (location=0) out vec4 f_color;

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

#define PROJECT_BINDING_POINT 2 // ************************************
// 36
struct Texture {
    mat4 textureToWorld;
    mat4 worldToTexture;
    vec4 bounds;
};
// 36
layout (std140, binding = PROJECT_BINDING_POINT) uniform ProjectBlock {
    Texture project_texture;
};
// ********************************************************************


const vec3 FLAT_BLUE = vec3(0.5,0.5,1.0);


float normalDepth(float d, float amp) {return (2.0 * d - 1.0) * amp;}

sampler2D depthSampler() {return u_sampler_array[0];}
sampler2D colorSampler() {return u_sampler_array[1];}

vec4 fetchDepth(ivec2 texel) {return texelFetch(depthSampler(), texel, 0);}
vec4 fetchColor(ivec2 texel) {return texelFetch(colorSampler(), texel, 0);}

ivec2 ce() {return (ivec2(gl_FragCoord.xy));}
ivec2 up() {return (ivec2(gl_FragCoord.xy) + ivec2( 0, 1));}
ivec2 ri() {return (ivec2(gl_FragCoord.xy) + ivec2( 1, 0));}
ivec2 dw() {return (ivec2(gl_FragCoord.xy) + ivec2( 0,-1));}
ivec2 le() {return (ivec2(gl_FragCoord.xy) + ivec2(-1, 0));}


void main() {

    vec3 color_out = vec3(FLAT_BLUE);

    ivec2 texel = ce();

    if(fetchColor(texel).a > 0.0) {
        float xlim = project_texture.bounds.z - project_texture.bounds.x - 1;
        float ylim = project_texture.bounds.w - project_texture.bounds.y - 1;
        float d = normalDepth(fetchDepth(texel).r, depth_amplitude);
        float hr = texel.x < xlim ? normalDepth(fetchDepth(ri()).r, depth_amplitude) : d;
        float hu = texel.y < ylim ? normalDepth(fetchDepth(up()).r, depth_amplitude) : d;
        float hl = texel.x > 0    ? normalDepth(fetchDepth(le()).r, depth_amplitude) : d;
        float hd = texel.y > 0    ? normalDepth(fetchDepth(dw()).r, depth_amplitude) : d;
        color_out = normalize(vec3(hl - hr, hd - hu, 2.0)) * 0.5 + 0.5;
    }

    f_color = vec4(color_out,1.0);

}