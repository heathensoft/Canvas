#version 440

layout (location=0) out vec4 f_color; // preview

in VS_OUT {
    vec2 uv;
} fs_in;

uniform sampler2D[6] u_sampler_array;
uniform sampler3D u_sampler_3d;
uniform int u_options;

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
    float amplitude; // depth amplitude
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

struct OutputOptions {
    bool output_normalmap; // precedence
    bool output_shadowmap;
    bool use_lighting;
    bool use_shadowmap;
    bool use_palette;
};

OutputOptions getOptions(int options) {
    return OutputOptions(
        bool(options & 1),
        bool(options & 2),
        bool(options & 4),
        bool(options & 8),
        bool(options & 16));
}

sampler2D colorSampler()        { return u_sampler_array[0]; }
sampler2D depthSampler()        { return u_sampler_array[1]; }
sampler2D specularSampler()     { return u_sampler_array[2]; }
sampler2D emissiveSampler()     { return u_sampler_array[3]; }
sampler2D shadowSampler()       { return u_sampler_array[4]; }
sampler2D normalSampler()       { return u_sampler_array[5]; }
sampler3D paletteSampler()      { return u_sampler_3d;       }

vec4 fetchColor(ivec2 texel)    { return texelFetch(colorSampler(),texel,0);    }
vec4 fetchDepth(ivec2 texel)    { return texelFetch(colorSampler(),texel,0);    }
vec4 fetchSpecular(ivec2 texel) { return texelFetch(specularSampler(),texel,0); }
vec4 fetchEmissive(ivec2 texel) { return texelFetch(emissiveSampler(),texel,0); }
vec4 fetchShadow(ivec2 texel)   { return texelFetch(shadowSampler(),texel,0);   }
vec4 fetchNormal(ivec2 texel)   { return texelFetch(normalSampler(),texel,0);   }
vec4 samplePalette(vec3 vec)    { return texture(paletteSampler(),vec);         }


const float CAMERA_VIRTUAL_Z = 512.0; // zoom == 1
const float SHINE_CONSTANT = 128.0;

void main() {

    vec4 color = vec4(1.0,1.0,1.0,1.0);

    vec2 uv = fs_in.uv;
    ivec2 texel = ivec2(gl_FragCoord.xy);

    vec4 source_color = fetchColor(texel);
    float source_alpha = source_color.a;


    OutputOptions options = getOptions(u_options);

    if(options.output_normalmap) {

        color = vec4(fetchNormal(texel).rgb,source_alpha);

    }
    else if(options.output_shadowmap) {

        // inverted is more intuative for the preview
        float red = 1 - (fetchShadow(texel).r);
        color = vec4(red,red,red,source_alpha);

    }
    else { // color preview

        if(options.use_lighting) {

            float shadow = 0.0;

            if(options.use_shadowmap) {

                shadow = fetchShadow(texel).r;
            }

            vec3 fragment_color = source_color.rgb;
            vec3 fragment_normal = normalize(fetchNormal(texel).xyz * 2.0 - 1);

            float fragment_z = (fetchDepth(texel).r * 2.0 - 1) * amplitude;
            vec2 fragment_xy = vec2(texture_bounds.xy + gl_FragCoord.xy);
            vec3 fragment_position = vec3(fragment_xy, fragment_z);

            vec3 camera_position = vec3(camera.position.xy, CAMERA_VIRTUAL_Z * camera.zoom);
            vec3 to_light_vector = vec3(light.position - fragment_position);
            vec3 to_light_direction = normalize(to_light_vector);


            // diffuse
            float diff = max(dot(fragment_normal, to_light_direction), 0.0);

            // specular
            float shine = (fetchSpecular(texel).r + 0.2) * SHINE_CONSTANT; // experiment with this
            vec3 to_eye_direction = normalize(camera_position - fragment_position);
            vec3 halfway_direction = normalize(to_light_direction + to_eye_direction);
            float spec = pow(max(dot(fragment_normal,halfway_direction),0.0),shine);
            spec *= energyConservation(shine);

            // emissive
            float emissive = fetchEmissive(texel).r;

            // attenuation (point light strength)
            Attenuation att = light.attenuation;
            float d = length(to_light_vector);
            float att_inv = 1.0 / (
            att.constant +
            att.linear * d +
            att.quadratic * d * d);

            vec3 light_a = light.color * light.ambience;// * (2 * att_inv);
            vec3 light_d = light.color * light.diffuse_strenght * att_inv;
            vec3 light_s = light.color * att_inv;

            vec3 frag_a = fragment_color * light_a;
            vec3 frag_d = fragment_color * light_d * diff * (1.0 - shadow);
            vec3 frag_s = fragment_color * light_s * spec * (1.0 - shadow);
            vec3 frag_e = fragment_color * emissive;
            vec3 frag_c = (frag_a + frag_d + frag_s + frag_e);

            color = vec4(clampColor(frag_c), source_alpha);


        } else {

            color = source_color;
        }

        if(options.use_palette) {

            color.rgb = samplePalette(color.rgb).rgb;

        }

    }


    f_color = color;
}