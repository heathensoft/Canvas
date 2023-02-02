#version 440

layout (location=0) out vec4 f_color;

uniform sampler2D u_sampler_2d;

in VS_OUT {
    vec2 uv;
    vec2 pos;
} fs_in;

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
    float diffuse; // diffuse strength
    Attenuation attenuation;
};

layout (std140, binding = LIGHT_BINDING_POINT) uniform LightBlock {
    Light light;
};

//********************************************************************


const float DARKEN = 0.8;
const float R2 = 16.0 * 16.0;

bool withinArea(vec2 point, vec4 area);
bool withinRadius(vec2 point, vec2 center, float r2);

void main() {

    vec4 color = texture(u_sampler_2d, fs_in.uv);

    if(!withinArea(fs_in.pos, tex.bounds)) {
        if(!withinRadius(fs_in.pos, light.position.xy, R2)) {
            color.rgb *= DARKEN;
        }
    }
    f_color = color;
}

bool withinArea(vec2 point, vec4 area) {
    float minX = area.x;
    float minY = area.y;
    float maxX = area.z;
    float maxY = area.w;
    float x = point.x;
    float y = point.y;
    return x > minX && y > minY && x < maxX && y < maxY;
}

bool withinRadius(vec2 point, vec2 center, float r2) {
    float dx = point.x - center.x;
    float dy = point.y - center.y;
    float dx2 = dx * dx;
    float dy2 = dy * dy;
    return dx2 + dy2 <= r2;
}

