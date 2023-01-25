package io.github.heathensoft.canvas.old.rendering;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.graphics.Vao;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 08/01/2023
 */


public class RectRenderBuffer implements Disposable {
    
    private final Vao vertexArrayObject;
    private final BufferObject vertexBuffer;
    private final BufferObject indexBuffer;
    
    
    public RectRenderBuffer() {
        this.vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
        this.vertexArrayObject = new Vao().bind();
        this.indexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        short[] indices = { 2, 1, 0, 0, 1, 3 };
        this.indexBuffer.bufferData(indices);
        int rect_vertices = 4;
        int vertex_size = 4;
        int buffer_size_bytes = rect_vertices * vertex_size * Float.BYTES;
        this.vertexBuffer.bind().bufferData(buffer_size_bytes);
        int pos_pointer = 0;
        int tex_pointer = 2 * Float.BYTES;
        int stride = vertex_size * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, pos_pointer);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, tex_pointer);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
    }
    
    public void upload(Coordinate coordinate) {
        float x0 = coordinate.x;
        float y0 = coordinate.y;
        float x1 = x0 + 1;
        float y1 = y0 + 1;
        upload(x0,y0,x1,y1);
    }
    
    public void upload(Area area) {
        float x0 = area.minX();
        float y0 = area.minY();
        float x1 = area.maxX() + 1;
        float y1 = area.maxY() + 1;
        upload(x0,y0,x1,y1);
    }

    public void upload(float x0, float y0, float x1, float y1) {
        vertexArrayObject.bind();
        vertexBuffer.bind();
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer vertices = stack.mallocFloat(16);
            vertices.put(x1).put(y0).put(1.0f).put(0.0f);
            vertices.put(x0).put(y1).put(0.0f).put(1.0f);
            vertices.put(x1).put(y1).put(1.0f).put(1.0f);
            vertices.put(x0).put(y0).put(0.0f).put(0.0f);
            vertexBuffer.bufferSubData(vertices.flip(),0);
        } glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    }
    
    
    @Override
    public void dispose() {
        Disposable.dispose(vertexArrayObject,vertexBuffer);
    }
}
