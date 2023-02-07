#version 440

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

#define CANVAS_SIDE 0
#define PREVIEW_SIDE 1
#define CONTOUR 2

layout (location=0) out vec4 f_color;

uniform sampler2D[3] u_sampler_array;
uniform vec2 u_framebuffer_size_inv;

in flat int instance_id;
in vec2 uv;

const vec4 CONTOUR_COLOR = vec4(0.0,1.0,0.0,1.0);
const vec2 ADJACENT_ARRAY[8] = {
vec2(-1.0, 1.0), vec2(0.0, 1.0), vec2(1.0, 1.0),
vec2(-1.0, 0.0),                 vec2(1.0, 0.0),
vec2(-1.0,-1.0), vec2(0.0,-1.0), vec2(1.0,-1.0)
};

float sampleContourRed(vec2 uv) {
    return texture(u_sampler_array[CONTOUR],uv).r;
}

void main() {

    vec4 color_out = texture(u_sampler_array[instance_id], uv);

    if(instance_id == PREVIEW_SIDE || brush.tool == BRUSH_TOOL_SAMPLER) {
        float contour_red = sampleContourRed(uv);
        if(contour_red > 0.0) {
            float accumulated = 0.0;
            for(int i = 0; i < 8; i++) {
                vec2 sample_uv = uv + (ADJACENT_ARRAY[i] * u_framebuffer_size_inv);
                float sample_red = sampleContourRed(sample_uv);
                if(sample_red > 0.0) accumulated += 1.0;
            }
            if(accumulated < 8.0) {
                color_out = CONTOUR_COLOR;
            }
        }
    }
    f_color = color_out;
}
