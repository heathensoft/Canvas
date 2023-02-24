package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.common.utils.DiscreteLine;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Set;


import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

/**
 * @author Frederik Dahl
 * 05/02/2023
 */


public class EditorGraphics implements Disposable {
    
    private static final int SPLIT_SCREEN_LEFT = 0;
    private static final int SPLIT_SCREEN_RIGHT = 1;
    private static final int SPLIT_SCREEN_CONTOUR = 2;
    
    private ENUM.Channel previousChannel;
    private final Editor editor;
    private final CanvasGrid grid;
    private final CanvasBackground background;
    
    private Vao texSpaceVAO;
    private BufferObject texSpaceVertexBuffer;
    private BufferObject texSpaceIndexBuffer;
    
    private Vao splitScreenVAO;
    private BufferObject splitScreenVertexBuffer;
    private BufferObject splitScreenIndexBuffer;
    private Framebuffer splitScreenFBO;
    
    
    public EditorGraphics(Editor editor) throws Exception {
        this.editor = editor;
        this.grid = new CanvasGrid();
        this.background = new CanvasBackground();
        this.previousChannel = editor.currentChannel();
        initializeSplitScreen();
        initializeTexSpace();
    }
    
    
    
    public void projectPipeline(Project project) {
        
        
        ENUM.Channel currentChannel = editor.currentChannel();
        Texture tex_intermediary = project.intermediaryBuffer().texture(0);
        Texture tex_backbuffer = project.backBuffer().texture(currentChannel.id);
        Texture tex_brush_overlay = project.brushOverlayBuffer().texture(0);
        Texture tex_detail = project.frontBuffer().texture(ENUM.Channel.DETAILS.id);
        Texture tex_volume = project.frontBuffer().texture(ENUM.Channel.VOLUME.id);
        Texture tex_emissive = project.frontBuffer().texture(ENUM.Channel.EMISSIVE.id);
        Texture tex_specular = project.frontBuffer().texture(ENUM.Channel.SPECULAR.id);
        Texture tex_occlusion = project.occlusionBuffer().texture(0);
        Texture tex_depth = project.depthBuffer().texture(0);
        Texture tex_normals = project.normalsBuffer().texture(0);
        Texture tex_shadows = project.shadowBuffer().texture(0);
        Texture tex_color = project.colorSourceTexture();
        Texture tex_palette = editor.currentPalette().texture();
        
        project.viewport();
        glDisable(GL_DEPTH_TEST);
        
        // TO BRUSH OVERLAY ***************************************************
    
        Brush brush = editor.brush();
        Coordinate cursor = editor.mouseCoordCurrent;
        int stroke_offset = brush.textureSize() / 2 - 1;
        Brush.StrokeBuffer strokeBuffer = brush.strokeBuffer();
        
        Framebuffer.bindDraw(project.brushOverlayBuffer());
        Framebuffer.clear();
        
        if (editor.isCurrentlyEditing()) {
            
            switch (brush.tool()) {
    
                case SAMPLER -> {
                    if (project.area().contains(cursor)) {
                        glDisable(GL_BLEND);
                        Shaders.areaToBrushProgram.use();
                        Vector4f area = MathLib.vec4(
                                cursor.x, cursor.y,cursor.x + 1,cursor.y + 1
                        );
                        Shaders.areaToBrushProgram.setUniform(Shaders.U_DRAG_AREA,area);
                        Shaders.areaToBrushProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
                        tex_color.bindToSlot(0);
                        texSpaceVAO.bind();
                        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
                    }
                }
                case FREE_HAND -> {
                    Set<Coordinate> points = editor.freeHandCoordinates;
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_ONE, GL_ONE);
                    glBlendEquation(GL_MAX);
                    Shaders.strokeToBrushProgram.use();
                    for (Coordinate point : points) {
                        int x0 = point.x - stroke_offset;
                        int y0 = point.y - stroke_offset;
                        strokeBuffer.put(x0,y0);
                    }
                    try (MemoryStack stack = MemoryStack.stackPush()){
                        IntBuffer buffer = stack.mallocInt(2);
                        buffer.put(0).put(1).flip();
                        Shaders.strokeToBrushProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
                    }
                    brush.texture().bindToSlot(0);
                    tex_color.bindToSlot(1);
                    strokeBuffer.upload();
                }
                case LINE_DRAW -> {
                    DiscreteLine line = editor.lineDrawCoordinates;
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_ONE, GL_ONE);
                    glBlendEquation(GL_MAX);
                    Shaders.strokeToBrushProgram.use();
                    for (Coordinate point : line) {
                        if (project.area().contains(point)) {
                            int x0 = point.x - stroke_offset;
                            int y0 = point.y - stroke_offset;
                            strokeBuffer.put(x0,y0);
                        }
                    }
                    try (MemoryStack stack = MemoryStack.stackPush()){
                        IntBuffer buffer = stack.mallocInt(2);
                        buffer.put(0).put(1).flip();
                        Shaders.strokeToBrushProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
                    }
                    brush.texture().bindToSlot(0);
                    tex_color.bindToSlot(1);
                    strokeBuffer.upload();
                }
                case DRAG_AREA -> {
                    if (project.area().intersects(editor.brushDragArea)) {
                        glDisable(GL_BLEND);
                        Shaders.areaToBrushProgram.use();
                        Vector4f area = MathLib.vec4(
                                editor.brushDragArea.minX(),
                                editor.brushDragArea.minY(),
                                editor.brushDragArea.maxX() +1,
                                editor.brushDragArea.maxY() +1
                        );
                        Shaders.areaToBrushProgram.setUniform(Shaders.U_DRAG_AREA,area);
                        Shaders.areaToBrushProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
                        tex_color.bindToSlot(0);
                        texSpaceVAO.bind();
                        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
                    }
                }
            }
            
        } else {
            
            switch (brush.tool()) {
    
                case SAMPLER, DRAG_AREA -> {
                    if (project.area().contains(cursor)) {
                        glDisable(GL_BLEND);
                        Shaders.areaToBrushProgram.use();
                        Vector4f area = MathLib.vec4(
                                cursor.x, cursor.y,cursor.x + 1,cursor.y + 1
                        );
                        Shaders.areaToBrushProgram.setUniform(Shaders.U_DRAG_AREA,area);
                        Shaders.areaToBrushProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
                        tex_color.bindToSlot(0);
                        texSpaceVAO.bind();
                        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
                    }
                }
                case FREE_HAND, LINE_DRAW -> {
                    if (editor.editableAreaBounds.contains(cursor)) {
                        glEnable(GL_BLEND);
                        glBlendFunc(GL_ONE, GL_ONE);
                        glBlendEquation(GL_MAX);
                        Shaders.strokeToBrushProgram.use();
                        int x0 = cursor.x - stroke_offset;
                        int y0 = cursor.y - stroke_offset;
                        strokeBuffer.put(x0,y0);
                        try (MemoryStack stack = MemoryStack.stackPush()){
                            IntBuffer buffer = stack.mallocInt(2);
                            buffer.put(0).put(1).flip();
                            Shaders.strokeToBrushProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
                        }
                        brush.texture().bindToSlot(0);
                        tex_color.bindToSlot(1);
                        strokeBuffer.upload();
                    }
                }
            }
        }
        
        
        
        // ********************************************************************
        glDisable(GL_BLEND);
        texSpaceVAO.bind();
        
        // BACK TO FRONT ******************************************************
    
        Framebuffer.bindDraw(project.frontBuffer());
        
        
        if (currentChannel != previousChannel) {
            Framebuffer.drawBuffer(previousChannel.id);
            Shaders.texturePassthroughProgram.use();
            Shaders.texturePassthroughProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
            project.backBuffer().texture(previousChannel.id).bindToSlot(0);
            glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
            previousChannel = currentChannel;
        }
        
        Framebuffer.drawBuffer(currentChannel.id);
        Shaders.backToFrontBufferProgram.use();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            Shaders.backToFrontBufferProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
        }
        tex_backbuffer.bindToSlot(0);
        tex_brush_overlay.bindToSlot(1);
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
        
        
        // DEPTH MIXING *******************************************************
    
        Framebuffer.bindDraw(project.depthBuffer());
        Shaders.textureDepthMixingProgram.use();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            Shaders.textureDepthMixingProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
        }
        tex_detail.bindToSlot(0);
        tex_volume.bindToSlot(1);
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
        
        // NORMAL MAPPING *****************************************************
    
        Framebuffer.bindDraw(project.normalsBuffer());
        Shaders.textureNormalsProgram.use();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            Shaders.textureNormalsProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
        }
        tex_depth.bindToSlot(0);
        tex_color.bindToSlot(1);
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    
        // AMBIENT_OCCLUSION **************************************************
    
        Framebuffer.bindDraw(project.occlusionBuffer());
        Shaders.textureAmbientOcclusionProgram.use();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            Shaders.textureAmbientOcclusionProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
        }
        tex_depth.bindToSlot(0);
        tex_normals.bindToSlot(1);
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    
        
        Framebuffer.bind(project.intermediaryBuffer());
        Shaders.textureSmoothenProgram.use();
        Shaders.textureSmoothenProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
        tex_occlusion.bindToSlot(0);
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
        Framebuffer.bind(project.occlusionBuffer());
        tex_intermediary.bindToSlot(0);
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
        
        
        // SHADOW MAPPING *****************************************************
        
        if (editor.previewShadow()) {
            Framebuffer.bindDraw(project.shadowBuffer());
            Shaders.textureShadowsProgram.use();
            Shaders.textureShadowsProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
            tex_depth.bindToSlot(0);
            glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    
            
            Framebuffer.bind(project.intermediaryBuffer());
            Shaders.textureSmoothenProgram.use();
            Shaders.textureSmoothenProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
            tex_shadows.bindToSlot(0);
            glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
            Framebuffer.bind(project.shadowBuffer());
            tex_intermediary.bindToSlot(0);
            glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
            
        }
        
        
        
    
        // LIGHTING / PREVIEW *************************************************
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        Framebuffer.bindDraw(project.previewBuffer());
        Framebuffer.clear();
        Shaders.textureLightingProgram.use();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(7);
            buffer.put(0).put(1).put(2).put(3).put(4).put(5).put(6).flip();
            Shaders.textureLightingProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
            Shaders.textureLightingProgram.setUniform1i(Shaders.U_SAMPLER_3D,7);
        }
        tex_color.bindToSlot(0);
        tex_depth.bindToSlot(1);
        tex_specular.bindToSlot(2);
        tex_emissive.bindToSlot(3);
        tex_shadows.bindToSlot(4);
        tex_normals.bindToSlot(5);
        tex_occlusion.bindToSlot(6);
        tex_palette.bindToSlot(7);
        
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
        
        // HDR / BLOOM ETC. --->>
        
    }
    
    public void drawToScreen() {
    
        glDisable(GL_DEPTH_TEST);
        ENUM.Channel channel = editor.currentChannel();
        Project project = editor.activeProject();
        
        // CLEAR ALL
        Framebuffer.bindDraw(splitScreenFBO);
        Framebuffer.drawBuffers(
                SPLIT_SCREEN_LEFT,
                SPLIT_SCREEN_RIGHT,
                SPLIT_SCREEN_CONTOUR);
        Framebuffer.viewport();
        Framebuffer.clear();
        
        // BACKGROUND TO LEFT
        glDisable(GL_BLEND);
        Framebuffer.drawBuffer(SPLIT_SCREEN_LEFT);
        background.draw();
        
        if (project != null) { // DRAW TEXTURES TO CANVAS SPACE
            
            Texture canvas = project.frontBuffer().texture(channel.id);
            Texture preview = project.previewBuffer().texture(0);
            Texture source_color = project.colorSourceTexture();
            Texture brush_overlay = project.brushOverlayBuffer().texture(0);
            
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
            glBlendEquation(GL_FUNC_ADD);
            
            Framebuffer.drawBuffers(
                    SPLIT_SCREEN_LEFT,
                    SPLIT_SCREEN_RIGHT,
                    SPLIT_SCREEN_CONTOUR);
            
            Shaders.textureToCanvasProgram.use();
            try (MemoryStack stack = MemoryStack.stackPush()){
                IntBuffer buffer = stack.mallocInt(4);
                buffer.put(0).put(1).put(2).put(3).flip();
                Shaders.textureToCanvasProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
            }
            canvas.bindToSlot(0);
            preview.bindToSlot(1);
            source_color.bindToSlot(2);
            brush_overlay.bindToSlot(3);
            splitScreenVAO.bind();
            glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
        }
        
        // DRAW GRID OVER LEFT
        Framebuffer.drawBuffer(SPLIT_SCREEN_LEFT);
        grid.draw(editor.camera());
        
        // DRAW CANVAS TO SCREEN
        
        glDisable(GL_BLEND);
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        
        Shaders.canvasToScreenProgram.use();
        Vector2f canvas_inv = MathLib.vec2();
        canvas_inv.set(1f/(editor.screenWidth()/2f),1f/editor.screenHeight());
        Shaders.canvasToScreenProgram.setUniform(Shaders.U_CANVAS_SIZE_INV,canvas_inv);
        Texture leftSide = splitScreenFBO.texture(SPLIT_SCREEN_LEFT);
        Texture rightSide = splitScreenFBO.texture(SPLIT_SCREEN_RIGHT);
        Texture contour = splitScreenFBO.texture(SPLIT_SCREEN_CONTOUR);
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(4);
            buffer.put(0).put(1).put(2).flip();
            Shaders.canvasToScreenProgram.setUniform1iv(Shaders.U_SAMPLER_ARRAY,buffer);
        }
        leftSide.bindToSlot(0);
        rightSide.bindToSlot(1);
        contour.bindToSlot(2);
        splitScreenVAO.bind();
        glDrawElementsInstanced(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0,2);
    }
    
    public void drawToBackbuffer(ENUM.Channel channel) {
        Project project = editor.activeProject();
        glDisable(GL_BLEND);
        texSpaceVAO.bind();
        Framebuffer.bindDraw(project.backBuffer());
        Framebuffer.viewport();
        Framebuffer.drawBuffer(channel.id);
        Shaders.texturePassthroughProgram.use();
        Shaders.texturePassthroughProgram.setUniform1i(Shaders.U_SAMPLER_2D,0);
        project.frontBuffer().texture(channel.id).bindToSlot(0);
        glDrawElements(GL_TRIANGLES,6,GL_UNSIGNED_SHORT,0);
    }
    
    public CanvasBackground background() {
        return background;
    }
    
    public CanvasGrid grid() {
        return grid;
    }
    
    
    public void readPixel(Coordinate coordinate) {
    
    }
    
    public int pixelValue() {
        return 255;
    }
    
    public void dispose() {
        Disposable.dispose(grid,background);
        disposeTextureSpace();
        disposeSplitScreen();
    }
    
    private void initializeTexSpace() {
        texSpaceIndexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        texSpaceVertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_STATIC_DRAW);
        texSpaceVAO = new Vao().bind();
        texSpaceIndexBuffer.bind();
        texSpaceIndexBuffer.bufferData(new short[]{ 2, 1, 0, 0, 1, 3 });
        texSpaceVertexBuffer.bind();
        texSpaceVertexBuffer.bufferData(new float[]{ 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f });
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }
    
    private void initializeSplitScreen() throws Exception {
        int half_width = editor.screenWidth() / 2;
        int screen_height = editor.screenHeight();
        splitScreenIndexBuffer = new BufferObject(GL_ELEMENT_ARRAY_BUFFER,GL_STATIC_DRAW);
        splitScreenVertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_STATIC_DRAW);
        short[] indices = { 2, 1, 0, 0, 1, 3 };
        float[] vertices = {
                0.0f ,-1.0f, 1.0f, 0.0f,
                -1.0f, 1.0f, 0.0f, 1.0f,
                0.0f , 1.0f, 1.0f, 1.0f,
                -1.0f,-1.0f, 0.0f, 0.0f,
        }; splitScreenVAO = new Vao().bind();
        splitScreenIndexBuffer.bind();
        splitScreenIndexBuffer.bufferData(indices);
        splitScreenVertexBuffer.bind();
        splitScreenVertexBuffer.bufferData(vertices);
        int posPointer = 0;
        int texPointer = 2 * Float.BYTES;
        int stride = 4 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, posPointer);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, texPointer);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        
        splitScreenFBO = new Framebuffer(half_width,screen_height);
        Framebuffer.bind(splitScreenFBO);
        for (int i = 0; i < 2; i++) {
            Texture colorTexture = Texture.generate2D(half_width,screen_height);
            colorTexture.bindToActiveSlot();
            colorTexture.wrapST(GL_CLAMP_TO_EDGE);
            colorTexture.filter(GL_LINEAR,GL_NEAREST);
            colorTexture.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED,false);
            Framebuffer.attachColor(colorTexture,i,true);
        }
        Texture contourTexture = Texture.generate2D(half_width,screen_height);
        contourTexture.bindToActiveSlot();
        contourTexture.wrapST(GL_CLAMP_TO_EDGE);
        contourTexture.filter(GL_LINEAR,GL_NEAREST);
        contourTexture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
        Framebuffer.attachColor(contourTexture,2,true);
        Framebuffer.drawBuffers(0,1,2);
        Framebuffer.checkStatus();
    }
    
    private void disposeTextureSpace() {
        Disposable.dispose(
                texSpaceVAO,
                texSpaceVertexBuffer,
                texSpaceIndexBuffer
        );
    }
    
    private void disposeSplitScreen() {
        Disposable.dispose(
                splitScreenVAO,
                splitScreenIndexBuffer,
                splitScreenVertexBuffer,
                splitScreenFBO);
    }
    
    
}
