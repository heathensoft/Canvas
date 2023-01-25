package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 24/01/2023
 */


public class CommonUniforms implements Disposable {
    
    public static final int BINDING_POINT = 0;
    public static final int STRUCT_SIZE_FLOAT = 44;
    private final BufferObject uniformBuffer;
    
    public CommonUniforms() {
        uniformBuffer = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW);
        uniformBuffer.bind();
        uniformBuffer.bufferData(STRUCT_SIZE_FLOAT * Float.BYTES);
        uniformBuffer.bindBufferBase(BINDING_POINT);
    }
    
    public void upload(
            OrthographicCamera camera,
            Vector4f texture_bounds,
            Vector2f mouse,
            float tStep,
            float amplitude) {
        
        uniformBuffer.bind();
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(STRUCT_SIZE_FLOAT);
            put(camera,buffer);
            put(texture_bounds,buffer);
            put(mouse,buffer);
            buffer.put(tStep).put(amplitude);
            uniformBuffer.bufferSubData(buffer.flip(),0);
        }
        
    }
    
    private void put(OrthographicCamera camera, FloatBuffer buffer) {
        put(camera.combined(),buffer);
        put(camera.combinedINV(),buffer);
        put(camera.position,buffer);
        buffer.put(camera.zoom);
    }
    
    private void put(Matrix4f mat4, FloatBuffer buffer) {
        mat4.get(buffer.position(),buffer);
        buffer.position(buffer.position() + 16);
    }
    
    private void put(Vector4f vec4, FloatBuffer buffer) {
        vec4.get(buffer.position(),buffer);
        buffer.position(buffer.position() + 4);
    }
    
    private void put(Vector3f vec3, FloatBuffer buffer) {
        vec3.get(buffer.position(),buffer);
        buffer.position(buffer.position() + 3);
    }
    
    private void put(Vector2f vec2, FloatBuffer buffer) {
        vec2.get(buffer.position(),buffer);
        buffer.position(buffer.position() + 2);
    }
    
    public void dispose() {
        Disposable.dispose(uniformBuffer);
    }
}
