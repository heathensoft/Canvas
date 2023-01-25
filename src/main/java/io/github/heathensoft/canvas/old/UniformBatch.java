package io.github.heathensoft.canvas.old;

import io.github.heathensoft.canvas.light.PointLight;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 09/01/2023
 */


public class UniformBatch implements Disposable {

    public static final int BINDING_POINT = 0;
    
    private final BufferObject uniformBuffer;
    private final int upload_offset_float;
    private final int dynamic_size_float;
    private final int total_size_float;
    
    public UniformBatch(Matrix4f fullscreen) {
        uniformBuffer = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW);
        uniformBuffer.bind();
        int num_mat4 = 3;
        int num_vec4 = 2;
        int mat4_size = 16;
        int vec4_size = 4;
        total_size_float = mat4_size * num_mat4 + vec4_size * num_vec4;
        upload_offset_float = mat4_size;
        dynamic_size_float = total_size_float - upload_offset_float;
        uniformBuffer.bufferData(total_size_float * Float.BYTES);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(mat4_size);
            fullscreen.get(buffer);
            uniformBuffer.bufferSubData(buffer,0);
        } uniformBuffer.bindBufferBase(BINDING_POINT);
    }
    
    public void upload(
            OrthographicCamera camera,
            Vector2f mouse,
            Vector4f textureBounds,
            PointLight light,
            float frame_time ) {
        
    }
    
    public void upload(OrthographicCamera projectCamera, Vector4f textureBounds, Vector4f mousePosition) {
        uniformBuffer.bind();
        Matrix4f project_combined = projectCamera.combined();
        Matrix4f project_combined_inv = projectCamera.combinedINV();
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(dynamic_size_float);
            put(project_combined,buffer);
            put(project_combined_inv,buffer);
            put(textureBounds,buffer);
            put(mousePosition,buffer);
            int offset_bytes = upload_offset_float * Float.BYTES;
            uniformBuffer.bufferSubData(buffer.flip(),offset_bytes);
        }
    }
    
    private void put(Matrix4f mat4, FloatBuffer buffer) {
        mat4.get(buffer.position(),buffer);
        buffer.position(buffer.position() + 16);
    }
    
    private void put(Vector4f vec4, FloatBuffer buffer) {
        vec4.get(buffer.position(),buffer);
        buffer.position(buffer.position() + 4);
    }
    
    public void dispose() {
        Disposable.dispose(uniformBuffer);
    }
    
    public int sizeFloat() {
        return total_size_float;
    }
}
