package io.github.heathensoft.canvas.brush;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 28/01/2023
 */


public class BrushUniforms implements Disposable {
    
    public static final int BINDING_POINT = 2;
    
    private final BufferObject uniformBuffer;
    
    public BrushUniforms() {
        uniformBuffer = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW);
        uniformBuffer.bind();
        uniformBuffer.bufferData((long) 8 * 4);
        uniformBuffer.bindBufferBase(BINDING_POINT);
    }
    
    public void upload(Brush brush) {
        uniformBuffer.bind();
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer floatBuffer = stack.mallocFloat(3);
            Color cColor = brush.contourColor();
            floatBuffer.put(cColor.r).put(cColor.g).put(cColor.b);
            uniformBuffer.bufferSubData(floatBuffer.flip(),0);
            IntBuffer intBuffer = stack.mallocInt(5);
            intBuffer.put(brush.textureSize()).put(brush.function().id);
            intBuffer.put(brush.colorValue()).put(brush.tool().id).put(brush.shape().id);
            uniformBuffer.bufferSubData(intBuffer.flip(),3 * Float.BYTES);
        }
    }
    
    public void dispose() {
        Disposable.dispose(uniformBuffer);
    }
}
