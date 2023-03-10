#version 440

layout (location=0) out vec4 f_color; // preview

uniform sampler2D[7] u_sampler_array;
uniform sampler3D u_sampler_3d;


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

struct PreviewOptions {
    bool output_depthmap; // precedence
    bool output_normalmap;
    bool output_shadowmap;
    bool use_lighting;
    bool use_shadowmap;
    bool use_palette;
};

PreviewOptions getOptions(int options) {
    return PreviewOptions (
        bool(options & 1),
        bool(options & 2),
        bool(options & 4),
        bool(options & 8),
        bool(options & 16),
        bool(options & 32));
}

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

#define LIGHT_BINDING_POINT 4 // **************************************

#define LIGHT_LEVELS 6
// 4
struct Attenuation {
    float constant;
    float linear;
    float quadratic;
    float std140_padding;
};
// 4 * LEVELS + 12
struct Light {
    Attenuation[LIGHT_LEVELS] att;
    vec3  position;
    float ambience;
    vec3  color;
    float diffuse;
    vec2 std140_padding;
    float brightness;
    float point_light;
};
// 4 * LEVELS + 12
layout (std140, binding = LIGHT_BINDING_POINT) uniform LightBlock {
    Light light;
};

bool usingPointLight(Light light) {
    return light.point_light != 0.0;
}

Attenuation attenuation(Light light) {
    return light.att[clamp(int(light.brightness), 0, LIGHT_LEVELS -1)];
}

vec3 clampColor(vec3 rgb) {
    return clamp(rgb, vec3(0.0, 0.0, 0.0), vec3(1.0, 1.0, 1.0));
}

float energyConservation(float shine) {
    return ( 8.0 + shine ) / ( 8.0 * 3.14159265 );
}

//*********************************************************************

sampler2D colorSampler()        { return u_sampler_array[0]; }
sampler2D depthSampler()        { return u_sampler_array[1]; }
sampler2D specularSampler()     { return u_sampler_array[2]; }
sampler2D emissiveSampler()     { return u_sampler_array[3]; }
sampler2D shadowSampler()       { return u_sampler_array[4]; }
sampler2D normalSampler()       { return u_sampler_array[5]; }
sampler2D occlusionSampler()    { return u_sampler_array[6]; }
sampler3D paletteSampler()      { return u_sampler_3d;       }

vec4 fetchColor(ivec2 texel)    { return texelFetch(colorSampler(),texel,0);    }
vec4 fetchDepth(ivec2 texel)    { return texelFetch(depthSampler(),texel,0);    }
vec4 fetchSpecular(ivec2 texel) { return texelFetch(specularSampler(),texel,0); }
vec4 fetchEmissive(ivec2 texel) { return texelFetch(emissiveSampler(),texel,0); }
vec4 fetchShadow(ivec2 texel)   { return texelFetch(shadowSampler(),texel,0);   }
vec4 fetchNormal(ivec2 texel)   { return texelFetch(normalSampler(),texel,0);   }
vec4 fetchOcclusion(ivec2 texel){ return texelFetch(occlusionSampler(),texel,0);}
vec4 samplePalette(vec3 vec)    { return texture(paletteSampler(),vec);         }


// You get some diffuse light even if the angle between
// the to-light and fragment normal is > 90 deg.
const float DIR_LIGHT_WRAP = 0.33334;
const float CAMERA_VIRTUAL_Z = 512.0; // zoom == 1
const float DEFAULT_SHINE_EXPONENT = 128.0;

//const vec3 SKY_COLOR = vec3(0.38, 0.44, 0.77);
const vec3 SKY_COLOR = vec3(0.44, 0.12, 0.97);
const vec3 GROUND_COLOR = vec3(0.33,0.67,0.18);

void main() {

    vec4 color = vec4(1.0,1.0,1.0,1.0);
    ivec2 texel = ivec2(gl_FragCoord.xy);
    vec4 source_color = fetchColor(texel);
    float source_alpha = source_color.a;

    PreviewOptions options = getOptions(int(preview_options));

    if(options.output_depthmap) {
        //color = vec4(fetchDepth(texel).rrr,source_alpha);
        color = vec4(fetchOcclusion(texel).rrr,source_alpha); // ---------------------------------------------------------

    }
    else if (options.output_normalmap){
        color = vec4(fetchNormal(texel).rgb,source_alpha);

    }
    else if(options.output_shadowmap) {
        float red = fetchShadow(texel).r;
        color = vec4(red,red,red,source_alpha);

    }
    else { // color preview

        if(options.use_lighting) {

            float shadow = 1.0; // no shadow
            if(options.use_shadowmap) {
                shadow = fetchShadow(texel).r;
            }

            vec3 fragment_color = source_color.rgb;
            vec3 normal_sample = fetchNormal(texel).xyz;
            vec3 fragment_normal = normalize(normal_sample * 2.0 - 1);
            float normal_y_factor = fragment_normal.y * 0.5 + 0.5;
            float depth_sample = fetchDepth(texel).r;
            float fragment_z = (depth_sample * 2.0 - 1) * depth_amplitude;
            vec2 fragment_xy = vec2(project_texture.bounds.xy + gl_FragCoord.xy);
            vec3 fragment_position = vec3(fragment_xy, fragment_z);
            vec3 camera_position = vec3(camera.position.xy, CAMERA_VIRTUAL_Z * camera.zoom);

            if(usingPointLight(light)) {
                vec3 to_light_vector = vec3(light.position - fragment_position);
                vec3 to_light_direction = normalize(to_light_vector);
                // diffuse
                float diff = max(dot(fragment_normal, to_light_direction), 0.0);
                // specular
                vec3 to_eye_direction = normalize(camera_position - fragment_position);
                vec3 halfway_direction = normalize(to_light_direction + to_eye_direction);
                float energy_conservation = energyConservation(DEFAULT_SHINE_EXPONENT);
                float spec_factor = pow(max(dot(fragment_normal, halfway_direction), 0.0), DEFAULT_SHINE_EXPONENT);
                float spec_sample = fetchSpecular(texel).r;
                // emissive
                float emis = fetchEmissive(texel).r;
                // ambience
                float ambi = fetchOcclusion(texel).r;
                // attenuation (point light strength)
                Attenuation att = attenuation(light);
                float d = length(to_light_vector);
                float att_inv = 1.0 / (att.constant + att.linear * d + att.quadratic * d * d);



                diff = diff * shadow;

                float spec = spec_factor * spec_sample * shadow;

                vec3 secondary_ambient_dir = normalize(vec3(0.0,0.999,0.0));

                float wrap = 0.33;
                float dott = clamp(dot(secondary_ambient_dir,fragment_normal) + wrap, 0.0, wrap + 1.0) / (wrap + 1.0);
                //float dott = clamp(dot(secondary_ambient_dir,fragment_normal), 0.0, 1.0);
                float influence = 0.25; // influence
                float intensity = 0.4;
                vec3 secondary_ambient_color = SKY_COLOR * intensity * dott;
                vec3 light_a = (light.color * light.ambience * ( ((att_inv) * (1.0 - influence)) / 2.0) + secondary_ambient_color * ( ((1.0 - att_inv) * influence) / 2.0)  );

                vec3 light_d = light.color * light.diffuse * att_inv;
                vec3 light_s = light.color * att_inv;
                vec3 frag_a = fragment_color * light_a * ambi;
                vec3 frag_d = fragment_color * light_d * diff;
                vec3 frag_s = fragment_color * light_s * spec;
                vec3 frag_e = fragment_color * emis;

                vec3 frag_c = (frag_a + frag_d + frag_s + frag_e);
                color = vec4(clampColor(frag_c), source_alpha);
            }
            else {
                vec3 to_light_direction = normalize(light.position);
                // diffuse
                float diff = clamp(dot(fragment_normal, to_light_direction) + DIR_LIGHT_WRAP, 0.0, DIR_LIGHT_WRAP + 1.0) / (DIR_LIGHT_WRAP + 1.0);
                // specular
                vec3 to_eye_direction = normalize(camera_position - fragment_position);
                vec3 halfway_direction = normalize(to_light_direction + to_eye_direction);
                float energy_conservation = energyConservation(DEFAULT_SHINE_EXPONENT);
                float spec_factor = energy_conservation * pow(max(dot(fragment_normal, halfway_direction), 0.0), DEFAULT_SHINE_EXPONENT);
                float spec_sample = fetchSpecular(texel).r;
                // emissive
                float emis = fetchEmissive(texel).r;
                // ambience
                float ambi = fetchOcclusion(texel).r;





                vec3 secondary_ambient_dir = normalize(vec3(0.0,0.999,0.0));
                float wrap = 0.33;
                float dott = clamp(dot(secondary_ambient_dir,fragment_normal) + wrap, 0.0, wrap + 1.0) / (wrap + 1.0);
                float ratio = 1.0 - clamp(dot(to_light_direction,vec3(0.0,0.0,1.0)),0.0,1.0); // * influence
                float intensity = 1.0;
                vec3 secondary_ambient_color = SKY_COLOR * intensity * dott; // intensity

                vec3 light_a = (light.color * light.ambience * (1.0 - ratio) + secondary_ambient_color * ratio);

                diff = diff * shadow * (1.0 - ratio);

                float spec = spec_factor * spec_sample * shadow * (1.0 - ratio);

                vec3 light_d = light.color * light.diffuse;
                vec3 light_s = light.color;
                vec3 frag_a = fragment_color * light_a * ambi;
                vec3 frag_d = fragment_color * light_d * diff;
                vec3 frag_s = fragment_color * light_s * spec;
                vec3 frag_e = fragment_color * emis * ratio * ratio; // ----this is fine for directional lights .

                vec3 frag_c = (frag_a + frag_d + frag_s + frag_e);
                color = vec4(clampColor(frag_c), source_alpha);
            }

        } else {

            color = source_color;
        }


        float gamma = 2.2;
        color.rgb = pow(color.rgb, vec3(1.0/gamma));

        if(options.use_palette) {

            color.rgb = samplePalette(color.rgb).rgb;

        }

    }

    f_color = color;
}