#version 440

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out GS_OUT {
    vec2 brush_uv;
} gs_out;

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

vec2 uv_to_ndc(vec2 uv) {
    float x = uv.x * 2 - 1;
    float y = uv.y * 2 - 1;
    return vec2(x,y);
}

const vec2 offset_uv[4] = {
    vec2(0.0,0.0),
    vec2(1.0,0.0),
    vec2(0.0,1.0),
    vec2(1.0,1.0)
};

void main() {

    vec2 bs = vec2(brush.texture_size);

    vec4 offset_pos[4] = {
        vec4(0.0 , 0.0 , 0.0, 0.0),
        vec4(bs.x, 0.0 , 0.0, 0.0),
        vec4(0.0 , bs.y, 0.0, 0.0),
        vec4(bs.x, bs.y, 0.0, 0.0)
    };

    vec4 bottom_left = gl_in[0].gl_Position;

    for(int i = 0; i < 4; i++) {
        vec4 corner = bottom_left + offset_pos[i];
        vec2 tex_position = vec4(project_texture.worldToTexture * corner).xy;
        gl_Position = vec4(uv_to_ndc(tex_position),0.0,1.0);
        gs_out.brush_uv = offset_uv[i];
        EmitVertex();
    }

    EndPrimitive();

}