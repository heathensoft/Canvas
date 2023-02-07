#version 440

layout (location=0) out vec4 f_color;

uniform sampler2D u_sampler_2d;
uniform vec4 u_drag_area;

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
// Brush

#define BRUSH_TOOL_SAMPLER 0
#define BRUSH_TOOL_FREE_HAND 1
#define BRUSH_TOOL_LINE_DRAW 2
#define BRUSH_TOOL_DRAG 3

#define BRUSH_SHAPE_ROUND 0
#define BRUSH_SHAPE_SQUARE 1

#define BRUSH_BINDING_POINT 2

struct Brush {
    vec3 contour_color;
    int texture_size;
    int function;
    int color_value;
    int tool;
    int shape;
};

layout (std140, binding = BRUSH_BINDING_POINT) uniform BrushBlock {
    Brush brush;
};

//********************************************************************


float fetchColorAlpha(ivec2 texel);
bool withinRect(vec2 point, vec4 area);
bool withinElipse(vec2 point, vec4 area);

void main() {

    f_color = vec4(1.0,0.0,0.0,1.0);

    if(fetchColorAlpha(ivec2(gl_FragCoord.xy)) <= 0.1) {

        discard;

    } else {

        if(brush.tool == BRUSH_TOOL_DRAG) {

            vec2 world_position = tex.bounds.xy + gl_FragCoord.xy;

            if(brush.shape == BRUSH_SHAPE_ROUND) {

                if(withinElipse(world_position, u_drag_area)) {
                    return;
                }
                else discard;

            } else if(brush.shape == BRUSH_SHAPE_SQUARE) {

                if(withinRect(world_position, u_drag_area)) {
                    return;
                }
                else discard;
            }
            else discard;
        }
        else discard;
    }


}

float fetchColorAlpha(ivec2 texel) {
    return texelFetch(u_sampler_2d,texel,0).a;
}

bool withinElipse(vec2 point, vec4 area) {
    // (x-h)^2/a^2 + (y-k)^2/b^2 <= 1
    float x = point.x;
    float y = point.y;
    float a = (area.z - area.x) / 2.0;
    float b = (area.w - area.y) / 2.0;
    float h = area.x + a;
    float k = area.y + b;
    float p = pow((x-h),2.0) / pow(a,2.0) + pow((y-k),2.0) / pow(b,2.0);
    return p <= 1.0;
}

bool withinRect(vec2 point, vec4 area) {
    float minX = area.x;
    float minY = area.y;
    float maxX = area.z;
    float maxY = area.w;
    float x = point.x;
    float y = point.y;
    return x > minX && y > minY && x < maxX && y < maxY;
}