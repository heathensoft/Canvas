#version 440

layout (points) in;
layout (triangle_strip, max_vertices = 4) out;

out GS_OUT {
    vec2 brush_uv;
} gs_out;

uniform mat4 u_combined;

const vec4 offset_pos[4] = vec4[4] ( // pixels (half of brush texture)
vec4(-16.0,-16.0, 0.0, 0.0),
vec4( 16.0,-16.0, 0.0, 0.0),
vec4(-16.0, 16.0, 0.0, 0.0),
vec4( 16.0, 16.0, 0.0, 0.0)
);

const vec2 offset_uv[4] = vec2[4] (
vec2(-1.0,-1.0),
vec2( 1.0,-1.0),
vec2(-1.0, 1.0),
vec2( 1.0, 1.0)
);

void main() {

    vec4 center = gl_in[0].gl_Position;

    for(int i = 0; i < 4; i++) {
        vec4 corner = center + offset_pos[i];
        gl_Position = u_combined * corner;
        gs_out.brush_uv = offset_uv[i];
        EmitVertex();
    }

    EndPrimitive();

}