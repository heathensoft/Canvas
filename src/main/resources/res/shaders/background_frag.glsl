#version 440

layout (location=0) out vec4 f_color;

layout (std140, binding = 0) uniform UniformBlock {
    mat4 fullscreen_camera_combined;
    mat4 project_camera_combined;
    mat4 project_camera_combined_inv;
    vec4 project_texture_bounds;
};

in VS_OUT {
    vec2 uv;
    vec2 pos;
} fs_in;

uniform sampler2D u_background_texture;

const float darken_outside = 0.8;

bool contains(vec2 point, vec4 bounds) {
    float minX = bounds.x;
    float minY = bounds.y;
    float maxX = bounds.z;
    float maxY = bounds.w;
    float x = point.x;
    float y = point.y;
    return x > minX && y > minY && x < maxX && y < maxY;
}

void main() {

    vec4 color = texture(u_background_texture,fs_in.uv);

    if(!contains(fs_in.pos, project_texture_bounds)) {
        color.rgb *= darken_outside;
    }

    f_color = color;

}
