
//********************************************************************
// Light

#define LIGHT_BINDING_POINT 1
#define LIGHT_BRIGHTNESS_LEVELS 6

struct Attenuation {
    float constant;
    float linear;
    float quadratic;
    float std140_padding;
};

struct Lignt {
    Attenuation[LIGHT_BRIGHTNESS_LEVELS] att;
    vec3  position;
    float ambience;
    vec3  color;
    float diffuse;
    vec2 std140_padding;
    float brightness;
    float point_light;
};

layout (std140, binding = LIGHT_BINDING_POINT) uniform LightBlock {
    Light light;
};

bool usingPointLight(Lignt light) {
    return light.point_light != 0.0;
}

Attenuation attenuation(Lignt light) {
    return light.att[clamp(int(light.brightness),0,LIGHT_BRIGHTNESS_LEVELS -1)];
}

float energyConservation(float shine) {
    return ( 16.0 + shine ) / ( 16.0 * 3.14159265 );
}

//********************************************************************

