#version 440

layout (location=0) out vec4 f_color;

in VS_OUT {
    vec2 uv;
    vec2 pos;
} fs_in;

uniform sampler2D u_sampler_2d;

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


const float DARKEN = 0.8;
const float R2 = 16.0 * 16.0;

bool withinTextureBounds(vec2 frag_world, vec4 bounds);
bool withinMouseRadius(vec2 frag_world, vec2 mouse);

void main() {

    vec4 color = texture(u_sampler_2d, fs_in.uv);

    if(!withinTextureBounds(fs_in.pos, texture_bounds)) {
        if(!withinMouseRadius(fs_in.pos, mouse_world)) {
            color.rgb *= DARKEN;
        }
    }
    f_color = color;
}

bool withinTextureBounds(vec2 frag_world, vec4 bounds) {
    float minX = bounds.x;
    float minY = bounds.y;
    float maxX = bounds.z;
    float maxY = bounds.w;
    float x = frag_world.x;
    float y = frag_world.y;
    return x > minX && y > minY && x < maxX && y < maxY;
}

bool withinMouseRadius(vec2 frag_world, vec2 mouse) {
    float dx = frag_world.x - mouse.x;
    float dy = frag_world.y - mouse.y;
    float dx2 = dx * dx;
    float dy2 = dy * dy;
    return dx2 + dy2 <= R2;
}

