package io.github.heathensoft.canvas;

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


public class Project implements Disposable, Comparable<Project> {
    
    private final int project_id;
    private String project_name;
    private final Vector4f bounds;
    private final Texture colorSource;
    private final Framebuffer brushOverlayBuffer;
    private final Framebuffer depthBuffer;
    private final Framebuffer shadowBuffer;
    private final Framebuffer normalsBuffer;
    private final Framebuffer previewBuffer;
    private final Framebuffer frontBuffer;
    private final Framebuffer backBuffer;
    private final UndoRedoManager undoRedoManager;
    
    Project(PngImporter.Textures sources, int id) throws Exception {
        this(default_project_name(),sources,id);
    }
    
    Project(String name, PngImporter.Textures sources, int id) throws Exception {
        
        colorSource = sources.color_source();
        bounds = new Vector4f(0.0f,0.0f,texturesWidth(),texturesHeight());
        undoRedoManager = new UndoRedoManager(this);
    
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
        Framebuffer.attachColor(front_buffer_details, Channel.DETAILS.id,true);
        Framebuffer.attachColor(front_buffer_volume, Channel.VOLUME.id,true);
        Framebuffer.attachColor(front_buffer_specular,Channel.SPECULAR.id,true);
        Framebuffer.attachColor(front_buffer_emissive,Channel.EMISSIVE.id,true);
        Framebuffer.drawBuffers(Channel.DETAILS.id,Channel.VOLUME.id,Channel.SPECULAR.id,Channel.EMISSIVE.id);
        Framebuffer.checkStatus();
    
        //***********************************************************************************************************
    
        backBuffer = new Framebuffer(texturesWidth(),texturesHeight());
        Framebuffer.bind(backBuffer);
        Framebuffer.attachColor(back_buffer_details,Channel.DETAILS.id,true);
        Framebuffer.attachColor(back_buffer_volume,Channel.VOLUME.id,true);
        Framebuffer.attachColor(back_buffer_specular,Channel.SPECULAR.id,true);
        Framebuffer.attachColor(back_buffer_emissive,Channel.EMISSIVE.id,true);
        Framebuffer.drawBuffers(Channel.DETAILS.id,Channel.VOLUME.id,Channel.SPECULAR.id,Channel.EMISSIVE.id);
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
        
        Texture brushOVL = Texture.generate2D(texturesWidth(),texturesHeight());
        brushOVL.bindToActiveSlot();
        brushOVL.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
        brushOVL.filter(GL_NEAREST,GL_NEAREST);
        brushOVL.clampToBorder();
    
        brushOverlayBuffer = new Framebuffer(texturesWidth(),texturesHeight());
        Framebuffer.bind(brushOverlayBuffer);
        Framebuffer.attachColor(brushOVL,0,true);
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
        Texture back_buffer_details =   backBuffer.texture(Channel.DETAILS.id);
        Texture back_buffer_volume =    backBuffer.texture(Channel.VOLUME.id);
        Texture back_buffer_specular =  backBuffer.texture(Channel.SPECULAR.id);
        Texture back_buffer_emissive =  backBuffer.texture(Channel.EMISSIVE.id);
    
        PngExporter exporter = new PngExporter(output_directory,project_name);
        exporter.exportColor(colorSource,overwrite);
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
    
    public UndoRedoManager undoRedoManager() {
        return undoRedoManager;
    }
    
    public Framebuffer brushOverlayBuffer() {
        return brushOverlayBuffer;
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
        return colorSource;
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
        return colorSource.width();
    }
    
    public int texturesHeight() {
        return colorSource.height();
    }
    
    public void dispose() {
        Disposable.dispose(
                colorSource,
                depthBuffer,
                shadowBuffer,
                normalsBuffer,
                previewBuffer,
                frontBuffer,
                backBuffer,
                brushOverlayBuffer,
                undoRedoManager
        );
    }
    
    public static Path default_output_directory() throws IOException {
        return External.USER_HOME();
    }
    
    public static String default_project_name() {
        return "untitled";
    }
    
    
    public int compareTo(Project o) {
        return Integer.compare(o.project_id,project_id);
    }
}
