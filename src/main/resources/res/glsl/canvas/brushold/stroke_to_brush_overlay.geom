#version 440

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out GS_OUT {
    vec2 brush_uv;
} gs_out;

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

const vec2 offset_uv[4] = {
    vec2(0.0,0.0),
    vec2(1.0,0.0),
    vec2(0.0,1.0),
    vec2(1.0,1.0)
};

void main() {

    float bs = float(brush.texture_size);

    vec4 offset_pos[4] = {
        vec4(0.0, 0.0, 0.0, 0.0),
        vec4(bs,  0.0, 0.0, 0.0),
        vec4(0.0, bs,  0.0, 0.0),
        vec4(bs,  bs,  0.0, 0.0)
    };

    vec4 bottom_left = gl_in[0].gl_Position;

    for(int i = 0; i < 4; i++) {
        vec4 corner = bottom_left + offset_pos[i];
        vec2 tex_position = vec4(tex.worldToTexture * corner).xy;
        gl_Position = vec4(uv_to_ndc(tex_position),0.0,1.0);
        gs_out.brush_uv = offset_uv[i];
        EmitVertex();
    }

    EndPrimitive();

}