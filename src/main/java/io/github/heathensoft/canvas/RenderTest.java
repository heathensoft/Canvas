package io.github.heathensoft.canvas;

import io.github.heathensoft.canvas.brush.Channel;
import io.github.heathensoft.canvas.io.PaletteImporter;
import io.github.heathensoft.canvas.io.PaletteOld;
import io.github.heathensoft.canvas.light.Attenuation;
import io.github.heathensoft.canvas.light.PointLight;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

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
    
    private final float depth_amplitude = 8.0f;
    private int preview_options = 4 + 8 + 16;
    
    public Project project;
    public PointLight light;
    public CanvasGrid canvasGrid;
    public SplitScreen splitScreen;
    public CanvasBackground background;
    
    public RenderTest(SplitScreen splitScreen, Project project) throws Exception {
        Shaders.initialize();
        TSpaceVTXBuffer.initialize();
        PaletteImporter.loadResources();
        this.splitScreen = splitScreen;
        this.project = project;
        this.canvasGrid = new CanvasGrid();
        this.background = new CanvasBackground();
        this.light = new PointLight(
                new Vector3f(0,16,20),
                new Color(0.91f,0.41f,0.47f,1.0f),
                Attenuation.ATT_600,
                0.7f,
                0.4f);
        
    }
    
    
    
    public void render(Vector2f mouse_world, float time_step) {
    
        
        splitScreen.camera().refresh();
        
        Texture tex_detail = project.frontBuffer().texture(Channel.DETAILS.idx);
        Texture tex_volume = project.frontBuffer().texture(Channel.VOLUME.idx);
        Texture tex_specular = project.frontBuffer().texture(Channel.SPECULAR.idx);
        Texture tex_emissive = project.frontBuffer().texture(Channel.EMISSIVE.idx);
        Texture tex_preview = project.previewBuffer().texture(0);
        Texture tex_normals = project.normalsBuffer().texture(0);
        Texture tex_shadows = project.shadowBuffer().texture(0);
        Texture tex_depth = project.depthBuffer().texture(0);
        Texture tex_color = project.colorSourceTexture();
    
        //************************************************************************
        
        // UPLOAD UNIFORMS
        
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
        
        //DEPTH MIXING
        
        Framebuffer.bindDraw(project.depthBuffer());
        Framebuffer.clear();
        
        textureDepthMixingProgram.use();
        textureDepthMixingProgram.setUniform1f(U_DETAIL_WEIGHT,0.5f);
    
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
        Framebuffer.clear();
        
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
        Framebuffer.clear();
        
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
        ColorPalette.get("polxel-42").texture().bindToSlot(6);
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
                tex_depth,
                tex_preview,
                tex_color
        );
        
        splitScreen.setDrawBufferLeft();
        canvasGrid.draw(splitScreen.camera());
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        splitScreen.drawFromSplitScreen();
        
    }
    
    
    
    public void dispose() {
        TSpaceVTXBuffer.dispose();
        Shaders.dispose();
        ColorPalette.disposeAll();
        Disposable.dispose(
                background,
                splitScreen,
                project,
                canvasGrid
        );
    }
}
