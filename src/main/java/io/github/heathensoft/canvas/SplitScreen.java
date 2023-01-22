package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static io.github.heathensoft.canvas.CanvasShaders.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

/**
 * @author Frederik Dahl
 * 21/01/2023
 */


public class SplitScreen implements Disposable {
    
    private final OrthographicCamera canvas_camera;
    private final Matrix4f screen_combined_inv;
    private final Matrix4f screen_combined;
    private final Matrix4f right_ndc_inv;
    private final Matrix4f left_ndc_inv;
    private final Vao vertexArrayObject;
    private final BufferObject indexBuffer;
    private final BufferObject vertexBuffer;
    private final ShaderProgram canvas_to_screen_shader;
    private final ShaderProgram texture_to_canvas_shader;
    private final Framebuffer framebuffer;
    
    public SplitScreen(int screen_width, int screen_height) throws Exception {
    
        Matrix4f view = MathLib.mat4().identity();
        screen_combined = new Matrix4f();
        view.lookAt(0,0,1, 0,0,-1, 0,1,0);
        screen_combined.ortho(0,screen_width,0,screen_height,0.01f,1);
        screen_combined.mul(view);
        screen_combined_inv = new Matrix4f(screen_combined).invert();
    
        int half_width = screen_width / 2;
        canvas_camera = new OrthographicCamera();
        canvas_camera.viewport.set(half_width, screen_height);
        canvas_camera.refresh();
        
        left_ndc_inv = new Matrix4f(
                
                 0.5f, 0.0f, 0.0f, 0.0f,
                 0.0f, 1.0f, 0.0f, 0.0f,
                 0.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.0f, 0.0f, 1.0f
                
        ).invert();
        
        right_ndc_inv = new Matrix4f(
                
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.0f, 0.0f, 1.0f
                
        ).invert();
    
        indexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_STATIC_DRAW);
    
        short[] indices = { 2, 1, 0, 0, 1, 3 };
        
        float[] vertices = {
                 0.0f,-1.0f, 1.0f, 0.0f, // Bottom right 0
                -1.0f, 1.0f, 0.0f, 1.0f, // Top left     1
                 0.0f, 1.0f, 1.0f, 1.0f, // Top right    2
                -1.0f,-1.0f, 0.0f, 0.0f, // Bottom left  3
        };
        
        Resources io = new Resources(SplitScreen.class);
        canvas_to_screen_shader = new ShaderProgram(
                io.asString(CANVAS_TO_SCREEN_SPACE_VERT),
                io.asString(CANVAS_TO_SCREEN_SPACE_FRAG));
        canvas_to_screen_shader.createUniform(U_SAMPLER_ARRAY);
        
        texture_to_canvas_shader = new ShaderProgram(
                io.asString(TEXTURE_TO_CANVAS_SPACE_VERT),
                io.asString(TEXTURE_TO_CANVAS_SPACE_FRAG));
        texture_to_canvas_shader.createUniform(U_SAMPLER_ARRAY);
        
        vertexArrayObject = new Vao().bind();
        indexBuffer.bind();
        indexBuffer.bufferData(indices);
        vertexBuffer.bind();
        vertexBuffer.bufferData(vertices);
        int posPointer = 0;
        int texPointer = 2 * Float.BYTES;
        int stride = 4 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, posPointer);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, texPointer);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
    
        framebuffer = new Framebuffer(half_width,screen_height);
        Framebuffer.bind(framebuffer);
        for (int i = 0; i < 2; i++) {
            Texture texture = Texture.generate2D(half_width,screen_height);
            texture.bindToActiveSlot();
            texture.wrapST(GL_CLAMP_TO_EDGE);
            texture.filter(GL_LINEAR,GL_NEAREST);
            texture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED,false);
            Framebuffer.attachColor(texture,i,true);
        } Framebuffer.drawBuffers(0,1);
        Framebuffer.checkStatus();
    }
    
    /**
     * Converts fullscreen mouse ndc to canvas world position
     * @param mouseNDC mouse ndc
     * @param dest the destination vector
     * @return the destination vector
     */
    public Vector2f unProjectMouse(Vector2f mouseNDC, Vector2f dest) {
        Vector3f v3 = MathLib.vec3(mouseNDC.x,mouseNDC.y,0);
        if (v3.x > 0) v3.mulProject(right_ndc_inv);
        else v3.mulProject(left_ndc_inv);
        dest.set(v3.x,v3.y);
        canvas_camera.unProject(dest);
        return dest;
    }
    
    // draw to fullscreen / default framebuffer
    public void drawFrom() {
        glDisable(GL_BLEND);
        canvas_to_screen_shader.use();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            canvas_to_screen_shader.setUniform1iv(U_SAMPLER_ARRAY,buffer);
            leftTexture().bindToSlot(0);
            rightTexture().bindToSlot(1);
        } vertexArrayObject.bind();
        glDrawElementsInstanced(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0,2);
    }
    
    // bind this framebuffer and draw to both
    public void drawTo(Texture frontBufferR8, Texture previewRGB8) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        texture_to_canvas_shader.use();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            texture_to_canvas_shader.setUniform1iv(U_SAMPLER_ARRAY,buffer);
            frontBufferR8.bindToSlot(0);
            previewRGB8.bindToSlot(1);
        } vertexArrayObject.bind();
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    }
    
    public void bindFramebufferDraw() {
        Framebuffer.bindDraw(framebuffer);
    }
    
    /** Bind framebuffer first! */
    public void setDrawBufferLeft() {
        Framebuffer.drawBuffer(0);
    }
    
    /** Bind framebuffer first! */
    public void setDrawBufferRight() {
        Framebuffer.drawBuffer(1);
    }
    
    /** Bind framebuffer first! */
    public void setDrawBuffersBoth() {
        Framebuffer.drawBuffers(0,1);
    }
    
    public Texture leftTexture() {
        return framebuffer.texture(0);
    }
    
    public Texture rightTexture() {
        return framebuffer.texture(1);
    }
    
    public OrthographicCamera canvasCamera() {
        return canvas_camera;
    }
    
    public Matrix4f fullscreenCombined() {
        return screen_combined;
    }
    
    public Matrix4f fullscreenCombinedInv() {
        return screen_combined_inv;
    }
    
    public void dispose() {
        Disposable.dispose(
                vertexArrayObject,
                indexBuffer,
                vertexBuffer,
                canvas_to_screen_shader,
                texture_to_canvas_shader,
                framebuffer
        );
    }
}