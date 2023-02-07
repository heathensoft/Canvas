#version 440

layout (location=0) out float f_color;

uniform sampler2D u_sampler_2d;
uniform vec4 u_drag_area;

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

#define BRUSH_BINDING_POINT 3 // **************************************

#define BRUSH_TOOL_SAMPLER 0
#define BRUSH_TOOL_FREE_HAND 1
#define BRUSH_TOOL_LINE_DRAW 2
#define BRUSH_TOOL_DRAG_AREA 3
#define BRUSH_NUM_TOOLS 4
#define BRUSH_SHAPE_ROUND 0
#define BRUSH_SHAPE_SQUARE 1
#define BRUSH_NUM_SHAPES 2
#define BRUSH_FUNC_NON 0
#define BRUSH_FUNC_SET 1
#define BRUSH_FUNC_ADD 2
#define BRUSH_FUNC_SUB 3
#define BRUSH_FUNC_MIX 4
#define BRUSH_FUNC_SMO 5
#define BRUSH_FUNC_SHA 6
#define BRUSH_FUNC_RAI 7
#define BRUSH_FUNC_LOW 8
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

float fetchColorAlpha(ivec2 texel) {
    return texelFetch(u_sampler_2d,texel,0).a;
}

float angle(vec2 vec) {
    return acos(dot(normalize(vec),vec2(1.0,0.0)));
}

float ellipseRadius(vec2 fromCenter, float a, float b) {
    float theta = angle(fromCenter);
    float denom_cos = pow(cos(theta),2.0) / (a * a);
    float denom_sin = pow(sin(theta),2.0) / (b * b);
    return 1 / sqrt(denom_cos + denom_sin);
}

float calcColorRaiseLowerSquare(vec2 point, vec4 area) {
    return 0.0;
}

float calcColorRaiseLowerRound(vec2 point, vec4 area) {
    float x = point.x;
    float y = point.y;
    float w = area.z - area.x;
    float h = area.w - area.y;
    float a = w / 2.0;
    float b = h / 2.0;
    float X = area.x + a; // center x
    float Y = area.y + b; // center y
    vec2 toPointVec = point - vec2(X, Y);
    float d = length(toPointVec);
    float r = ellipseRadius(toPointVec,a,b);
    return 1.0 - smoothstep(0.0,r,d);
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


void main() {

    float color = 0.0;

    if(fetchColorAlpha(ivec2(gl_FragCoord.xy)) > 0.1) {

        vec2 world_pos = project_texture.bounds.xy + gl_FragCoord.xy;

        if(brush.tool == BRUSH_TOOL_DRAG_AREA) {

            if(brush.shape == BRUSH_SHAPE_ROUND) {

                if(brush.function == BRUSH_FUNC_RAI || brush.function == BRUSH_FUNC_LOW && brush.size > 2) {

                    color = calcColorRaiseLowerRound(world_pos, u_drag_area);

                } else {
                    if(withinElipse(world_pos,u_drag_area)) color = 1.0;
                }
            } else if(brush.shape == BRUSH_SHAPE_SQUARE) {

                if(brush.function == BRUSH_FUNC_RAI || brush.function == BRUSH_FUNC_LOW && brush.size > 2) {

                    color = calcColorRaiseLowerSquare(world_pos, u_drag_area);
                } else {
                    if(withinRect(world_pos,u_drag_area)) color = 1.0;
                }
            }
        } else if(brush.tool == BRUSH_TOOL_SAMPLER) {

            if(withinRect(world_pos, u_drag_area)) color = 1.0;
        }
    }
    f_color = color;
}

