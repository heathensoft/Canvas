#version 440

layout (location=0) in vec4 a_pos;
layout (location=1) in vec2 a_uv;


out flat int instance_id;
out vec2 uv;


void main() {

    instance_id = gl_InstanceID;
    vec2 offset = vec2(1.0,0.0) * float(instance_id);
    gl_Position = a_pos + vec4(offset,0.0,0.0);
    uv = a_uv;
}
