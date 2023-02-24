package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * @author Frederik Dahl
 * 09/01/2023
 */


public class CanvasBackground implements Disposable {
    
    private final BufferObject vertexBuffer;
    private final BufferObject indexBuffer;
    private final Texture backgroundTexture;
    private final Vao vertexArrayObject;
    
    public CanvasBackground() throws Exception {
        backgroundTexture = Texture.generate2D(2,2);
        backgroundTexture.bindToActiveSlot();
        backgroundTexture.wrapST(GL_REPEAT);
        backgroundTexture.filter(GL_LINEAR_MIPMAP_LINEAR,GL_NEAREST);
        backgroundTexture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED,true);
        int bg_color_0 = Color.valueOf("808080").toIntBits();
        int bg_color_1 = Color.valueOf("c0c0c0").toIntBits();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer pixels = stack.mallocInt(4);
            pixels.put(bg_color_0).put(bg_color_1);
            pixels.put(bg_color_1).put(bg_color_0);
            backgroundTexture.uploadData(pixels.flip());
        } backgroundTexture.generateMipmap();
        vertexArrayObject = new Vao().bind();
        indexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_STATIC_DRAW);
        float[] vertices = {1.0f,-1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f,-1.0f};
        short[] indices = {2, 1, 0, 0, 1, 3};
        indexBuffer.bind();
        indexBuffer.bufferData(indices);
        vertexBuffer.bind();
        vertexBuffer.bufferData(vertices);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }
    
    public void draw() {
        glDisable(GL_BLEND);
        Shaders.canvasBackgroundProgram.use();
        Shaders.canvasBackgroundProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
        backgroundTexture.bindToSlot(0);
        vertexArrayObject.bind();
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
        Texture.unbindActiveSlot(backgroundTexture.target());
    }
    
    public void setColors(Color c0, Color c1) {
        int bg_color_0 = c0.toIntBits();
        int bg_color_1 = c1.toIntBits();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer pixels = stack.mallocInt(4);
            pixels.put(bg_color_0).put(bg_color_1);
            pixels.put(bg_color_1).put(bg_color_0);
            pixels.flip();
            glTexSubImage2D(GL_TEXTURE_2D,0,0,0,
            2,2,GL_RGBA,GL_UNSIGNED_BYTE,pixels);
        }
    }
    
    @Override
    public void dispose() {
        Disposable.dispose(
                backgroundTexture,
                vertexArrayObject,
                vertexBuffer,
                indexBuffer);
    }
}
