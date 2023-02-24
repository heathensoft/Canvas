#version 440

layout (location=0) out vec4 f_color;

uniform sampler2D u_sampler_2d;

in VS_OUT {
    vec2 uv;
    vec2 pos;
} fs_in;

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

float energyConservation(float shine) {
    return ( 16.0 + shine ) / ( 16.0 * 3.14159265 );
}
//*********************************************************************


const float DARKEN = 0.8;
const float R2 = 16.0 * 16.0;

bool withinArea(vec2 point, vec4 area);
bool withinRadius(vec2 point, vec2 center, float r2);

void main() {

    vec4 color = texture(u_sampler_2d, fs_in.uv);

    if(!withinRadius(fs_in.pos, light.position.xy, R2)) {
        color.rgb *= DARKEN;
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

