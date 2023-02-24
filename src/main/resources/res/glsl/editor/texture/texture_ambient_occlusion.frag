#version 440

#define KERNEL_SIZE 128

layout (location=0) out float f_color;

in VS_OUT {
    vec2 uv;
} fs_in;

uniform sampler2D[2] u_sampler_array;
uniform vec3[KERNEL_SIZE] u_occlusion_samples;


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

vec3 fetchNormal(ivec2 texel) { return texelFetch(u_sampler_array[1],texel,0).rgb; }

float sampleDepth(vec2 uv) { return texture(u_sampler_array[0],uv).r; }

float depth_to_world_z(float depth_sample, float world_amplitude) {
    return (depth_sample * 2.0 - 1.0) * world_amplitude;
}
float world_z_to_depth(float world_z, float world_amplitude) {
    return ((world_z / world_amplitude) + 1.0) * 0.5;
}

void main() {

    float occlusion = 0.0;

    ivec2 texel = ivec2(gl_FragCoord.xy);
    vec3 normal_map_sample = fetchNormal(texel);
    vec3 fragment_normal = normalize(normal_map_sample * 2.0 - 1.0);
    float normal_z_component = (fragment_normal.z + 1.0) * 0.5;

    float radius = 64.0;
    vec2 rotation_vec = vec2(1.0,1.0);
    bool even_x = texel.x % 2 == 0;
    bool even_y = texel.y % 2 == 0;
    if(even_x) rotation_vec.x *= -1.0;
    if(even_y) rotation_vec.y *= -1.0;

    vec4 bounds = project_texture.bounds;
    vec2 texture_size_inv = 1 / vec2(bounds.zw - bounds.xy);
    float fragment_depth = sampleDepth(fs_in.uv);
    float fragment_world_z = depth_to_world_z(fragment_depth,depth_amplitude);
    vec3 fragment_world_pos = vec3(texel + bounds.xy,fragment_world_z);

    for(int i = 0; i < KERNEL_SIZE; i++) {
        vec3 occlusion_sample = u_occlusion_samples[i];
        vec2 xy_component = vec2(occlusion_sample.xy * rotation_vec);
        float z_component = sqrt(1.0 - pow(xy_component.x, 2.0));
        vec3 occ_sample_normal = normalize(vec3(xy_component, z_component));
        float occ_sample_length = occlusion_sample.z * radius;
        vec3 occ_sample_world_vec = normalize(occ_sample_normal + fragment_normal) * occ_sample_length;
        vec3 occ_sample_world_pos = fragment_world_pos + occ_sample_world_vec;
        vec2 sample_uv = occ_sample_world_pos.xy * texture_size_inv;
        float sample_depth = sampleDepth(sample_uv);
        float occ_pos_depth = world_z_to_depth(occ_sample_world_pos.z,depth_amplitude);
        occlusion += (sample_depth >= (occ_pos_depth + 0.00125) ? 1.0 : 0.0);
    }


    float occlusion_color = 1.0 - (occlusion / KERNEL_SIZE);
    occlusion_color *= occlusion_color;
    f_color = (5.0 * occlusion_color + 2.0 * normal_z_component + fragment_depth) / 8.0;
}