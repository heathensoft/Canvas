package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.graphics.Vao;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 22/01/2023
 */


public class TSpaceVTXBuffer {
    
    private static Vao vertexArrayObject;
    private static BufferObject vertexBuffer;
    private static BufferObject indexBuffer;
    
    private static boolean initialized;
    
    private TSpaceVTXBuffer() {}
    
    public static void initialize() {
        if (!initialized) {
            initialized = true;
            indexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
            vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_STATIC_DRAW);
            vertexArrayObject = new Vao().bind();
            indexBuffer.bind();
            indexBuffer.bufferData(new short[]{ 2, 1, 0, 0, 1, 3 });
            vertexBuffer.bind();
            vertexBuffer.bufferData(new float[]{ 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f });
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);
        }
    }
    
    public static void transferElements() {
        vertexArrayObject.bind();
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    }
    
    public static void dispose() {
        if (initialized) {
            Disposable.dispose(
                    vertexArrayObject,
                    vertexBuffer,
                    indexBuffer
            ); initialized = false;
        }
        
    }
}
