#version 440

layout (location=0) out vec4 f_color; // shadowmap

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


vec4 sampleDepth(vec2 uv) {return texture(u_sampler_2d, uv);}

void main() {

    float t_width = texture_bounds.z - texture_bounds.x;
    float t_height = texture_bounds.w - texture_bounds.y;
    vec2 t_size = vec2(t_width,t_height);
    vec2 uv = vec2(gl_FragCoord / t_size);

    // just output 0.0 temporarily (no shadows)
    float depth_red = sampleDepth(uv).r;
    depth_red *= 0.000001;

    f_color = vec4(depth_red,depth_red,depth_red,1.0);

}