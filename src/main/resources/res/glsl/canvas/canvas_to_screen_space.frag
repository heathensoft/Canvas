#version 440

layout (location=0) out vec4 f_color;

in flat int instance_id;
in vec2 uv;

uniform sampler2D[2] u_sampler_array;

void main() {

    f_color = texture(u_sampler_array[instance_id], uv);

}
