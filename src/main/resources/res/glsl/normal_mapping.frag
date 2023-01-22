#version 440

layout (location=0) out vec4 f_color;


layout (std140, binding = 0) uniform CommonBlock {
    mat4 screen_camera_combined;
    mat4 canvas_camera_combined;
    mat4 canvas_camera_combined_inv;
    vec4 canvas_texture_bounds;
    vec4 mouse_position_canvas;
};


uniform float u_amplitude;
uniform sampler2D[2] u_sampler_array;

const vec3 FLAT_BLUE = vec3(0.5,0.5,1.0);



float normalDepth(float d, float amp) {return (2.0 * d - 1.0) * amp;}

sampler2D depthSampler() {return u_sampler_array[0];}
sampler2D colorSampler() {return u_sampler_array[1];}

vec4 sampleColor(ivec2 texel) {return texelFetch(colorSampler(), texel, 0);}
vec4 sampleDepth(ivec2 texel) {return texelFetch(depthSampler(), texel, 0);}

ivec2 ce() {return (ivec2(gl_FragCoord.xy));}
ivec2 up() {return (ivec2(gl_FragCoord.xy) + ivec2( 0, 1));}
ivec2 ri() {return (ivec2(gl_FragCoord.xy) + ivec2( 1, 0));}
ivec2 dw() {return (ivec2(gl_FragCoord.xy) + ivec2( 0,-1));}
ivec2 le() {return (ivec2(gl_FragCoord.xy) + ivec2(-1, 0));}


void main() {

    vec3 color_out = vec3(FLAT_BLUE);

    ivec2 texel = ce();

    if(sampleColor(texel).a > 0.0) {
        float xlim = canvas_texture_bounds.z - canvas_texture_bounds.x - 1;
        float ylim = canvas_texture_bounds.w - canvas_texture_bounds.y - 1;
        float d = normalDepth(sampleDepth(texel).r, u_amplitude);
        float hr = texel.x < xlim ? normalDepth(sampleDepth(ri()).r, u_amplitude) : d;
        float hu = texel.y < ylim ? normalDepth(sampleDepth(up()).r, u_amplitude) : d;
        float hl = texel.x > 0    ? normalDepth(sampleDepth(le()).r, u_amplitude) : d;
        float hd = texel.y > 0    ? normalDepth(sampleDepth(dw()).r, u_amplitude) : d;
        color_out = normalize(vec3(hl - hr, hd - hu, 2.0)) * 0.5 + 0.5;
    }


    f_color = vec4(color_out,1.0);

}