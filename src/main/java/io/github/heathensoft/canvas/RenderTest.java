package io.github.heathensoft.canvas;

import io.github.heathensoft.canvas.brush.Brush;
import io.github.heathensoft.canvas.brush.StrokeRenderBuffer;
import io.github.heathensoft.canvas.light.Attenuation;
import io.github.heathensoft.canvas.light.PointLight;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static io.github.heathensoft.canvas.Shaders.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 26/01/2023
 */


    /*
    struct OutputOptions {
    bool output_normalmap; // precedence
    bool output_shadowmap;
    bool use_lighting;
    bool use_shadowmap;
    bool use_palette;
};
     */

public class RenderTest implements Disposable {
    
    public final float depth_amplitude = 8.0f;
    private int preview_options = 4 + 8 + 16;
    
    public Channel channel;
    public Brush brush;
    public Project project;
    public PointLight light;
    public CanvasGrid canvasGrid;
    public SplitScreen splitScreen;
    public CanvasBackground background;
    public StrokeRenderBuffer strokeRenderBuffer;
    
    public RenderTest(SplitScreen splitScreen, Project project) throws Exception {
        Shaders.initialize();
        TSpaceVTXBuffer.initialize();
        ColorPalette.loadResources();
        this.splitScreen = splitScreen;
        this.project = project;
        this.canvasGrid = new CanvasGrid();
        this.background = new CanvasBackground();
        this.light = new PointLight(
                new Vector3f(0,16,20),
                new Color(0.99f,0.99f,0.99f,1.0f),
                Attenuation.ATT_3250,
                0.8f,
                0.6f);
        
        this.strokeRenderBuffer = new StrokeRenderBuffer(8);
        this.brush = new Brush(
                Brush.Shape.ROUND,
                Brush.Tool.FREE_HAND,
                Brush.Function.MIX,
                16
        );
        
        this.channel = Channel.DETAILS;
        
    }
    
    
    
    public void render(Vector2f mouse_world, float time_step) {
    
        
        splitScreen.camera().refresh();
        
        // using volume for backbuffer
        Texture tex_back = project.backBuffer().texture(Channel.VOLUME.id);
        
        Texture tex_brush_overlay = project.brushOverlayBuffer().texture(0);
        Texture tex_detail = project.frontBuffer().texture(Channel.DETAILS.id);
        Texture tex_volume = project.frontBuffer().texture(Channel.VOLUME.id);
        Texture tex_specular = project.frontBuffer().texture(Channel.SPECULAR.id);
        Texture tex_emissive = project.frontBuffer().texture(Channel.EMISSIVE.id);
        Texture tex_preview = project.previewBuffer().texture(0);
        Texture tex_normals = project.normalsBuffer().texture(0);
        Texture tex_shadows = project.shadowBuffer().texture(0);
        Texture tex_depth = project.depthBuffer().texture(0);
        Texture tex_color = project.colorSourceTexture();
    
        //************************************************************************
        
        // UPLOAD UNIFORMS
        
        brushUniforms.upload(brush);
        lightUniforms.upload(light);
        commonUniforms.upload(
                splitScreen.camera(),
                project.bounds(),
                mouse_world,
                time_step,
                depth_amplitude
        );
        
        project.viewport();
        glDisable(GL_BLEND);
    
        //************************************************************************
    
        //TO BRUSH OVERLAY
        
        Framebuffer.bindDraw(project.brushOverlayBuffer());
        Framebuffer.clear();
        strokeToBrushProgram.use();
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            strokeToBrushProgram.setUniform1iv(U_SAMPLER_ARRAY,buffer);
            brush.texture().bindToSlot(0);
            tex_color.bindToSlot(1);
        }
        
        int p_x = (int)(mouse_world.x - brush.textureSize() / 2f);
        int p_y = (int)(mouse_world.y - brush.textureSize() / 2f);
        
        strokeRenderBuffer.put(p_x,p_y);
        strokeRenderBuffer.upload();
    
        //************************************************************************
    
        // BACK TO FRONT (USING BRUSH OVERLAY)
    
        Framebuffer.bindDraw(project.frontBuffer());
        Framebuffer.drawBuffer(Channel.VOLUME.id);
        backToFrontBufferProgram.use();
    
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            backToFrontBufferProgram.setUniform1iv(U_SAMPLER_ARRAY,buffer);
            tex_back.bindToSlot(0);
            tex_brush_overlay.bindToSlot(1);
        }
        
        TSpaceVTXBuffer.transferElements();
    
        //************************************************************************
        
        
        //DEPTH MIXING
        
        Framebuffer.bindDraw(project.depthBuffer());
        //Framebuffer.clear();
        
        textureDepthMixingProgram.use();
        textureDepthMixingProgram.setUniform1f(U_DETAIL_WEIGHT,0.99f);
    
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            textureDepthMixingProgram.setUniform1iv(U_SAMPLER_ARRAY,buffer);
            tex_detail.bindToSlot(0);
            tex_volume.bindToSlot(1);
        }
    
        TSpaceVTXBuffer.transferElements();
    
        //************************************************************************
        
        //NORMAL MAPPING
    
        Framebuffer.bindDraw(project.normalsBuffer());
        //Framebuffer.clear();
        
        textureNormalsProgram.use();
    
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            textureNormalsProgram.setUniform1iv(U_SAMPLER_ARRAY,buffer);
            tex_depth.bindToSlot(0);
            tex_color.bindToSlot(1);
        }
        
        TSpaceVTXBuffer.transferElements();
    
        //************************************************************************
        
        // SHADOW MAPPING
        
        Framebuffer.bindDraw(project.shadowBuffer());
        //Framebuffer.clear();
        
        textureShadowsProgram.use();
        textureShadowsProgram.setUniform1i(U_SAMPLER_2D,0);
        tex_depth.bindToSlot(0);
    
        TSpaceVTXBuffer.transferElements();
    
        //************************************************************************
        
        // LIGHTING / PREVIEW
        
        glEnable(GL_BLEND);
        
        Framebuffer.bindDraw(project.previewBuffer());
        Framebuffer.clear();
        
        textureLightingProgram.use();
        textureLightingProgram.setUniform1i(U_OPTIONS,preview_options);
        textureLightingProgram.setUniform1i(U_SAMPLER_3D,6);
        ColorPalette.get("nanner").texture().bindToSlot(6);
        // upload palette here
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(6);
            buffer.put(0).put(1).put(2).put(3).put(4).put(5).flip();
            textureLightingProgram.setUniform1iv(U_SAMPLER_ARRAY,buffer);
            tex_color.bindToSlot(0);
            tex_depth.bindToSlot(1);
            tex_specular.bindToSlot(2);
            tex_emissive.bindToSlot(3);
            tex_shadows.bindToSlot(4);
            tex_normals.bindToSlot(5);
        }
    
        TSpaceVTXBuffer.transferElements();
    
        //************************************************************************
        
        // Split screen
        
        splitScreen.bindFramebufferDraw();
        splitScreen.setDrawBuffersBoth();
        Framebuffer.viewport();
        Framebuffer.clear();
        
        splitScreen.setDrawBufferLeft();
        background.draw();
        
        splitScreen.setDrawBuffersBoth();
        splitScreen.drawToSplitScreen(
                tex_volume,
                tex_preview,
                tex_color,
                tex_brush_overlay
        );
        
        splitScreen.setDrawBufferLeft();
        canvasGrid.draw(splitScreen.camera());
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        splitScreen.drawFromSplitScreen();
        
    }
    
    
    
    public void dispose() {
        /*
        try {
            PngExporter exporter = new PngExporter(External.USER_HOME("desktop","ritual"),"ritual");
            exporter.exportPreview(project.previewBuffer().texture(0),false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
         */
        TSpaceVTXBuffer.dispose();
        Shaders.dispose();
        ColorPalette.disposeAll();
        Disposable.dispose(
                background,
                splitScreen,
                project,
                canvasGrid,
                brush,
                strokeRenderBuffer
        );
    }
}
