package io.github.heathensoft.canvas.old.rendering;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.graphics.Vao;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 08/01/2023
 */


public class BrushRenderBuffer implements Disposable {

    private final Vao vertexArrayObject;
    private final BufferObject vertexBuffer;
    private final FloatBuffer vertices;
    private final int vertex_capacity;
    private int count;
    
    
    public BrushRenderBuffer(int vertex_capacity) {
        this.vertex_capacity = vertex_capacity;
        this.vertices = MemoryUtil.memAllocFloat(vertex_capacity * 2);
        this.vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        this.vertexArrayObject = new Vao().bind();
        this.vertexBuffer.bind().bufferData((long) 2 * vertex_capacity * Float.BYTES);
        glVertexAttribPointer(0,2,GL_FLOAT,false,2 * Float.BYTES,0);
        glEnableVertexAttribArray(0);
    }
    
    
    public void put(float x, float y) {
        if (count < vertex_capacity) {
            vertices.put(x).put(y);
            count++;
        }
    }
    
    
    public void upload() {
        if (count > 0) {
            vertices.flip();
            vertexArrayObject.bind();
            vertexBuffer.bind();
            vertexBuffer.bufferSubData(vertices,0);
            glDrawArrays(GL_POINTS,0,count);
            vertices.clear();
            count = 0;
        }
    }
    
    
    @Override
    public void dispose() {
        if (vertices != null) MemoryUtil.memFree(vertices);
        Disposable.dispose(vertexArrayObject,vertexBuffer);
    }
}
