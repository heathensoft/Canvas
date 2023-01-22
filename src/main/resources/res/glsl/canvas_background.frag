#version 440

layout (location=0) out vec4 f_color;

layout (std140, binding = 0) uniform CommonBlock {
    mat4 screen_camera_combined;
    mat4 canvas_camera_combined;
    mat4 canvas_camera_combined_inv;
    vec4 canvas_texture_bounds;
    vec4 mouse_position_canvas;
};

in VS_OUT {
    vec2 uv;
    vec2 pos;
} fs_in;

uniform sampler2D u_sampler;

const float DARKEN = 0.8;
const float R2 = 16.0 * 16.0;

bool withinTextureBounds(vec2 frag_world, vec4 bounds);
bool withinMouseRadius(vec2 frag_world, vec2 mouse);

void main() {

    vec4 color = texture(u_sampler, fs_in.uv);

    if(!withinTextureBounds(fs_in.pos, canvas_texture_bounds)) {
        if(!withinMouseRadius(fs_in.pos, mouse_position_canvas.xy)) {
            color.rgb *= DARKEN;
        }
    }
    f_color = color;
}

bool withinTextureBounds(vec2 frag_world, vec4 bounds) {
    float minX = bounds.x;
    float minY = bounds.y;
    float maxX = bounds.z;
    float maxY = bounds.w;
    float x = frag_world.x;
    float y = frag_world.y;
    return x > minX && y > minY && x < maxX && y < maxY;
}

bool withinMouseRadius(vec2 frag_world, vec2 mouse) {
    float dx = frag_world.x - mouse.x;
    float dy = frag_world.y - mouse.y;
    float dx2 = dx * dx;
    float dy2 = dy * dy;
    return dx2 + dy2 <= R2;
}

