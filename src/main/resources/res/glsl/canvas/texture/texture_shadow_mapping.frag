#version 440

layout (location=0) out vec4 f_color; // shadowmap

in VS_OUT {
    vec2 uv;
} fs_in;

uniform sampler2D u_sampler_2d; // depthmap

//********************************************************************
// Common

#define COMMON_BINDING_POINT 0

struct Camera {
    mat4 combined;
    mat4 combined_inv;
    vec3 position;
    float zoom;
};

layout (std140, binding = COMMON_BINDING_POINT) uniform CommonBlock {
    Camera camera;
    vec4 texture_bounds;
    vec2 mouse_world;
    float tStep;
    float amplitude;
};

//********************************************************************
// Light

#define LIGHT_BINDING_POINT 1

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
    float padding;
};

struct Light {
    vec3  position;
    float ambience;
    vec3  color;
    float diffuse_strenght;
    Attenuation attenuation;
};

layout (std140, binding = LIGHT_BINDING_POINT) uniform LightBlock {
    Light light;
};

float energyConservation(float shine) {
    return ( 16.0 + shine ) / ( 16.0 * 3.14159265 );
}

vec3 clampColor(vec3 rgb) {
    return clamp(rgb, vec3(0.0, 0.0, 0.0), vec3(1.0, 1.0, 1.0));
}

//********************************************************************

#define PI_HALF 1.5707963268
#define SAMPLE_DENSITY 4.0
#define MAX_SAMPLES 128

const vec3 PLANE_NORMAL = vec3(0.0,0.0,1.0);

vec4 sampleDepth(vec2 uv) {return texture(u_sampler_2d, uv);}

void main() {

    vec2 tex_size = vec2(texture_bounds.zw - texture_bounds.xy);
    float fragment_depth_value = sampleDepth(fs_in.uv).r;
    float fragment_z = (fragment_depth_value * 2.0 - 1) * amplitude;
    vec2 fragment_xy = vec2(texture_bounds.xy + gl_FragCoord.xy);
    vec3 fragment_pos = vec3(fragment_xy, fragment_z);

    vec3 to_light_vec = vec3(light.position - fragment_pos);
    vec3 to_light_dir = normalize(to_light_vec);

    float cosAngle = dot(PLANE_NORMAL, to_light_dir);
    float A = abs(PI_HALF - acos(cosAngle)); // theta
    float a = amplitude - fragment_z; // dist from frag_z to top depth amp
    float sinA = sin(A);
    float C = PI_HALF - A;
    float b = a / sinA;
    float c = b * sin(C);

    int num_samples = int(min(MAX_SAMPLES,int(round(c * SAMPLE_DENSITY))));
    float sample_delta = b / num_samples;
    vec3 move_vec = vec3(to_light_dir) * sample_delta;
    vec3 sample_pos = vec3(gl_FragCoord.xy,0.0); // z does not matter i think

    float shadow_add = 1.0 / num_samples;
    float shadow = 0.0;
    float sample_depth;
    vec2 sample_uv;

    for(int i = 0; i < num_samples; i++) {
        sample_pos += move_vec;
        sample_uv = sample_pos.xy / tex_size;
        sample_depth = sampleDepth(sample_uv).r;
        if(sample_depth > fragment_depth_value) {
            shadow += shadow_add;
        }
    }


    f_color = vec4(shadow, shadow, shadow, 1.0);

}