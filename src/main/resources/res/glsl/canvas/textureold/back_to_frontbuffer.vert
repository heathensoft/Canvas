#version 440

layout (location=0) in vec2 a_uv;

out VS_OUT {
    float brush_color;
} vs_out;

//********************************************************************
// Brush

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

vec2 uv_to_ndc(vec2 uv) {
    float x = uv.x * 2 - 1;
    float y = uv.y * 2 - 1;
    return vec2(x,y);
}

void main() {

    vs_out.brush_color = float((brush.color_value & 255) / 255.0);
    gl_Position = vec4(uv_to_ndc(a_uv),0.0,1.0);

}