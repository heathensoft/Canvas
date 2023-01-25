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

float energyConservation(float shine) {
    return ( 16.0 + shine ) / ( 16.0 * 3.14159265 );
}

//********************************************************************