
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
    float preview_opttions; // cast to int
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
// ********************************************************************
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
#define BRUSH_BINDING_POINT 3 // **************************************

#define BRUSH_TOOL_SAMPLER 0
#define BRUSH_TOOL_FREE_HAND 1
#define BRUSH_TOOL_LINE_DRAW 2
#define BRUSH_TOOL_DRAG_AREA 3
#define BRUSH_NUM_TOOLS 4
#define BRUSH_SHAPE_ROUND 0
#define BRUSH_SHAPE_SQUARE 1
#define BRUSH_NUM_SHAPES 2
#define BRUSH_FUNCTION_NON 0
#define BRUSH_FUNCTION_SET 1
#define BRUSH_FUNCTION_ADD 2
#define BRUSH_FUNCTION_SUB 3
#define BRUSH_FUNCTION_MIX 4
#define BRUSH_FUNCTION_SMO 5
#define BRUSH_FUNCTION_SHA 6
#define BRUSH_FUNCTION_RAI 7
#define BRUSH_FUNCTION_LOW 8
#define BRUSH_NUM_FUNCTIONS 9
// 8
struct Brush {
    ivec2 texture_size;
    int function;
    int color;
    int tool;
    int shape;
    int size;
    int std140_padding;
};
// 8
layout (std140, binding = BRUSH_BINDING_POINT) uniform BrushBlock {
    Brush brush;
};
//*********************************************************************
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
struct Lignt {
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

bool usingPointLight(Lignt light) {
    return light.point_light != 0.0;
}

Attenuation attenuation(Lignt light) {
    return light.att[clamp(int(light.brightness), 0, LIGHT_LEVELS -1)];
}

float energyConservation(float shine) {
    return ( 16.0 + shine ) / ( 16.0 * 3.14159265 );
}
//*********************************************************************