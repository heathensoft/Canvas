package io.github.heathensoft.canvas.light;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 23/01/2023
 */


public class LightUniforms implements Disposable {
    
    public static final int BINDING_POINT = 1;
    
    private final BufferObject uniformBuffer;
    
    public LightUniforms() {
        uniformBuffer = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW);
        uniformBuffer.bind();
        uniformBuffer.bufferData((long) PointLight.sizeFloat() * Float.BYTES);
        uniformBuffer.bindBufferBase(BINDING_POINT);
    }
    
    public void upload(PointLight light) {
        uniformBuffer.bind();
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(PointLight.sizeFloat());
            uniformBuffer.bufferSubData(light.get(buffer).flip(),0);
        }
    }
    
    public void dispose() {
        Disposable.dispose(uniformBuffer);
    }
}
