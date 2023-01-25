package io.github.heathensoft.canvas;

import io.github.heathensoft.canvas.brush.Channel;
import io.github.heathensoft.canvas.io.PngExporter;
import io.github.heathensoft.canvas.io.PngImporter;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureFormat;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import org.joml.Vector4f;

import java.io.IOException;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

/**
 *
 * Eventually make the project extend Interactable.
 *
 * @author Frederik Dahl
 * 24/01/2023
 */


public class Project implements Disposable {
    
    private final int project_id;
    private String project_name;
    private final Vector4f bounds;
    private final Texture color_source;
    private final Framebuffer depthBuffer;
    private final Framebuffer shadowBuffer;
    private final Framebuffer normalsBuffer;
    private final Framebuffer previewBuffer;
    private final Framebuffer frontBuffer;
    private final Framebuffer backBuffer;
    
    Project(PngImporter.Textures sources, int id) throws Exception {
        this(default_project_name(),sources,id);
    }
    
    Project(String name, PngImporter.Textures sources, int id) throws Exception {
        
        color_source = sources.color_source();
        bounds = new Vector4f(0.0f,0.0f,texturesWidth(),texturesHeight());
    
        Texture front_buffer_details = sources.front_buffer_details();
        Texture front_buffer_volume = sources.front_buffer_volume();
        Texture front_buffer_specular = sources.front_buffer_specular();
        Texture front_buffer_emissive = sources.front_buffer_emissive();
        Texture back_buffer_details = sources.back_buffer_details();
        Texture back_buffer_volume = sources.back_buffer_volume();
        Texture back_buffer_specular = sources.back_buffer_specular();
        Texture back_buffer_emissive = sources.back_buffer_emissive();
    
        //***********************************************************************************************************
    
        frontBuffer = new Framebuffer(texturesWidth(),texturesHeight());
        Framebuffer.bind(frontBuffer);
        Framebuffer.attachColor(front_buffer_details, Channel.DETAILS.idx,true);
        Framebuffer.attachColor(front_buffer_volume, Channel.VOLUME.idx,true);
        Framebuffer.attachColor(front_buffer_specular,Channel.SPECULAR.idx,true);
        Framebuffer.attachColor(front_buffer_emissive,Channel.EMISSIVE.idx,true);
        Framebuffer.drawBuffers(Channel.DETAILS.idx,Channel.VOLUME.idx,Channel.SPECULAR.idx,Channel.EMISSIVE.idx);
        Framebuffer.checkStatus();
    
        //***********************************************************************************************************
    
        backBuffer = new Framebuffer(texturesWidth(),texturesHeight());
        Framebuffer.bind(backBuffer);
        Framebuffer.attachColor(back_buffer_details,Channel.DETAILS.idx,true);
        Framebuffer.attachColor(back_buffer_volume,Channel.VOLUME.idx,true);
        Framebuffer.attachColor(back_buffer_specular,Channel.SPECULAR.idx,true);
        Framebuffer.attachColor(back_buffer_emissive,Channel.EMISSIVE.idx,true);
        Framebuffer.drawBuffers(Channel.DETAILS.idx,Channel.VOLUME.idx,Channel.SPECULAR.idx,Channel.EMISSIVE.idx);
        Framebuffer.checkStatus();
        
        //***********************************************************************************************************
        
        Texture depth_map = Texture.generate2D(texturesWidth(),texturesHeight());
        depth_map.bindToActiveSlot();
        depth_map.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
        depth_map.filter(GL_NEAREST,GL_NEAREST);
        depth_map.clampToEdge();
        
        depthBuffer = new Framebuffer(texturesWidth(),texturesHeight());
        Framebuffer.bind(depthBuffer);
        Framebuffer.attachColor(depth_map,0,true);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
    
        //***********************************************************************************************************
    
        Texture shadow_map = Texture.generate2D(texturesWidth(),texturesHeight());
        shadow_map.bindToActiveSlot();
        shadow_map.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
        shadow_map.filter(GL_NEAREST,GL_NEAREST);
        shadow_map.clampToEdge();
        
        shadowBuffer = new Framebuffer(texturesWidth(),texturesHeight());
        Framebuffer.bind(shadowBuffer);
        Framebuffer.attachColor(shadow_map,0,true);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
    
        //***********************************************************************************************************
    
        Texture normal_map = Texture.generate2D(texturesWidth(), texturesHeight());
        normal_map.bindToActiveSlot();
        normal_map.allocate(TextureFormat.RGB8_UNSIGNED_NORMALIZED,false);
        normal_map.filter(GL_NEAREST,GL_NEAREST);
        normal_map.clampToEdge();
    
        normalsBuffer = new Framebuffer(texturesWidth(),texturesHeight());
        Framebuffer.bind(normalsBuffer);
        Framebuffer.attachColor(normal_map,0,true);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
    
        //***********************************************************************************************************
    
        // Todo: generate mipmaps on edit (using: GL_LINEAR_MIPMAP_LINEAR)
        Texture preview = Texture.generate2D(texturesWidth(), texturesHeight());
        preview.bindToActiveSlot();
        preview.allocate(TextureFormat.RGBA8_UNSIGNED_NORMALIZED,true);
        preview.filter(GL_LINEAR,GL_NEAREST);
        preview.clampToEdge();
    
        previewBuffer = new Framebuffer(texturesWidth(),texturesHeight());
        Framebuffer.bind(previewBuffer);
        Framebuffer.attachColor(preview,0,true);
        Framebuffer.drawBuffer(0);
        Framebuffer.checkStatus();
    
        //***********************************************************************************************************
    
        project_name = name;
        project_id = id;
    }
    
    public void save(Path output_directory, boolean overwrite) throws Exception {
        
        Texture preview =               previewBuffer.texture(0);
        Texture depth_map =             depthBuffer.texture(0);
        Texture normal_map =            normalsBuffer.texture(0);
        Texture back_buffer_details =   backBuffer.texture(Channel.DETAILS.idx);
        Texture back_buffer_volume =    backBuffer.texture(Channel.VOLUME.idx);
        Texture back_buffer_specular =  backBuffer.texture(Channel.SPECULAR.idx);
        Texture back_buffer_emissive =  backBuffer.texture(Channel.EMISSIVE.idx);
    
        PngExporter exporter = new PngExporter(output_directory,project_name);
        exporter.exportColor(color_source,overwrite);
        exporter.exportPreview(preview,overwrite);
        exporter.exportDepth(depth_map,overwrite);
        exporter.exportNormals(normal_map,overwrite);
        exporter.exportDetails(back_buffer_details,overwrite);
        exporter.exportVolume(back_buffer_volume,overwrite);
        exporter.exportSpecular(back_buffer_specular,overwrite);
        exporter.exportEmissive(back_buffer_emissive,overwrite);
    }
    
    public void save(boolean overwrite) throws Exception {
        save(default_output_directory(),overwrite);
    }
    
    public void viewport() {
        Engine.get().window().viewport().set(0,0,texturesWidth(),texturesHeight());
    }
    
    public Framebuffer depthBuffer() {
        return depthBuffer;
    }
    
    public Framebuffer shadowBuffer() {
        return shadowBuffer;
    }
    
    public Framebuffer normalsBuffer() {
        return normalsBuffer;
    }
    
    public Framebuffer previewBuffer() {
        return previewBuffer;
    }
    
    public Framebuffer frontBuffer() {
        return frontBuffer;
    }
    
    public Framebuffer backBuffer() {
        return backBuffer;
    }
    
    public Texture colorSourceTexture() {
        return color_source;
    }
    
    public int projectID() {
        return project_id;
    }
    
    public String projectName() {
        return project_name;
    }
    
    public Vector4f bounds() {
        return bounds;
    }
    
    public void setProjectName(String name) {
        this.project_name = name;
    }
    
    public int texturesWidth() {
        return color_source.width();
    }
    
    public int texturesHeight() {
        return color_source.height();
    }
    
    public void dispose() {
        Disposable.dispose(
                color_source,
                depthBuffer,
                shadowBuffer,
                normalsBuffer,
                previewBuffer,
                frontBuffer,
                backBuffer
        );
    }
    
    public static Path default_output_directory() throws IOException {
        return External.USER_HOME();
    }
    
    public static String default_project_name() {
        return "untitled";
    }
}
