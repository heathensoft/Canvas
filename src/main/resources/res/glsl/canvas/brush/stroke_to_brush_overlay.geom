#version 440

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out GS_OUT {
    vec2 brush_uv;
} gs_out;

//********************************************************************
// Common

#define COMMON_BINDING_POINT 0

struct Camera {
    mat4 combined;
    mat4 combined_inv;
    vec3 position;
    float zoom;
};

layout (std140, binding = COMMON_BINDING_POINT) uniform CommonBlock {
    Camera camera;
    vec4 texture_bounds;
    vec2 mouse_world;
    float tStep;
    float amplitude;
};

//********************************************************************
// Brush

#define BRUSH_BINDING_POINT 2

struct Brush {
    vec4 contour_color;
    int texture_size;
    int function;
    int color_value;
    int std140_padding;
};

layout (std140, binding = BRUSH_BINDING_POINT) uniform BrushBlock {
    Brush brush;
};

//********************************************************************

const vec2 offset_uv[4] = {
vec2(-1.0, -1.0),
vec2(1.0, -1.0),
vec2(-1.0, 1.0),
vec2(1.0, 1.0)
};

void main() {

    float bs = float(brush.texture_size);

    vec4 offset_pos[4] = {
    vec4(0.0, 0.0, 0.0, 0.0),
    vec4(bs, 0.0, 0.0, 0.0),
    vec4(0.0, bs, 0.0, 0.0),
    vec4(bs, bs, 0.0, 0.0)
    };

    vec4 bottom_left = gl_in[0].gl_Position;

    for(int i = 0; i < 4; i++) {
        vec4 corner = bottom_left + offset_pos[i];
        gl_Position = camera.combined * corner;
        gs_out.brush_uv = offset_uv[i];
        EmitVertex();
    }

    EndPrimitive();

}