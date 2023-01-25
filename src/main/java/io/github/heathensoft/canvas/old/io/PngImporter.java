package io.github.heathensoft.canvas.old.io;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Image;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureFormat;
import io.github.heathensoft.jlib.lwjgl.graphics.surface.DepthMap8;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11.GL_NEAREST;


/**
 * The PngImporter is reusable after disposal (unlike most other disposables)
 * @author Frederik Dahl
 * 17/01/2023
 */


public class PngImporter implements Disposable {
    
    private Image diffuse_image;
    private Image depth_image;
    private Image specular_image;
    private Image emissive_image;
    private Status status;
    private String name;
    
    public record Textures(Texture color_source,
                           Texture front_buffer_depth,
                           Texture front_buffer_specular,
                           Texture front_buffer_emissive,
                           Texture back_buffer_depth,
                           Texture back_buffer_specular,
                           Texture back_buffer_emissive) { }
    
    public enum Status {
        INCOMPLETE("Color image missing",false),
        COMPLETE_READY("All images loaded",true),
        INCOMPLETE_READY("Incomplete, ready",true),
        SIZES_NOT_MATCHING("Image sizes not matching",false);
        public final String description;
        public final boolean ready;
        Status(String description, boolean ready) {
            this.description = description;
            this.ready = ready;
        }
    }
    
    public PngImporter() {
        status = Status.INCOMPLETE;
        name = "untitled";
    }
    
    public void importColorImage(Path path) throws Exception {
        ByteBuffer buffer = new Resources().toBufferExternal(path);
        Image image = new Image(buffer,true);
        MemoryUtil.memFree(buffer);
        if (diffuse_image != null) {
            diffuse_image.dispose();
        } diffuse_image = image;
        status = validate();
        name = resolveName(path);
    }
    
    public void importDepthImage(Path path) throws Exception {
        ByteBuffer buffer = new Resources().toBufferExternal(path);
        Image image = new Image(buffer,true);
        MemoryUtil.memFree(buffer);
        if (depth_image != null) {
            depth_image.dispose();
        } depth_image = image;
        status = validate();
    }
    
    public void importSpecularImage(Path path) throws Exception {
        ByteBuffer buffer = new Resources().toBufferExternal(path);
        Image image = new Image(buffer,true);
        MemoryUtil.memFree(buffer);
        if (specular_image != null) {
            specular_image.dispose();
        } specular_image = image;
        status = validate();
    }
    
    public void loadEmissiveImage(Path path) throws Exception {
        ByteBuffer buffer = new Resources().toBufferExternal(path);
        Image image = new Image(buffer,true);
        MemoryUtil.memFree(buffer);
        if (emissive_image != null) {
            emissive_image.dispose();
        } emissive_image = image;
        status = validate();
    }
    
    private Status validate() {
        if (diffuse_image == null) {
            return Status.INCOMPLETE;
        } int num_loaded = 1;
        int width = diffuse_image.width();
        int height = diffuse_image.height();
        if (depth_image != null) {
            if (depth_image.width() == width && depth_image.height() == height) {
                num_loaded++;
            } else return Status.SIZES_NOT_MATCHING;
        } if (specular_image != null) {
            if (specular_image.width() == width && specular_image.height() == height) {
                num_loaded++;
            } else return Status.SIZES_NOT_MATCHING;
        } if (emissive_image != null) {
            if (emissive_image.width() == width && emissive_image.height() == height) {
                num_loaded++;
            } else return Status.SIZES_NOT_MATCHING;
        } if (num_loaded == 4) return Status.COMPLETE_READY;
        return Status.INCOMPLETE_READY;
    }
    
    /**
     * Generates the texture needed by the editor.
     * Remember to call dispose on the importer when the images
     * are no longer needed.
     * @return the textures
     */
    public Textures generateTextures() {
        if (status.ready) {
    
            Texture color_source;
            Texture front_buffer_depth;
            Texture front_buffer_specular;
            Texture front_buffer_emissive;
            Texture back_buffer_depth;
            Texture back_buffer_specular;
            Texture back_buffer_emissive;
    
            final int WIDTH = diffuse_image.width();
            final int HEIGHT = diffuse_image.height();
    
            color_source = Texture.generate2D(WIDTH,HEIGHT);
            color_source.bindToActiveSlot();
            color_source.allocate(diffuse_image.format(),false);
            color_source.filter(GL_NEAREST,GL_NEAREST);
            color_source.clampToEdge();
            color_source.uploadData(diffuse_image.data());
            
            
            if (depth_image == null) {
                DepthMap8 depthMap = new DepthMap8(diffuse_image);
                ByteBuffer buffer = MemoryUtil.memAlloc(depthMap.size());
                buffer.put(depthMap.get()).flip();
                front_buffer_depth = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_depth.bindToActiveSlot();
                front_buffer_depth.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_depth.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_depth.clampToEdge();
                front_buffer_depth.uploadData(buffer);
                back_buffer_depth = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_depth.bindToActiveSlot();
                back_buffer_depth.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_depth.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_depth.clampToEdge();
                back_buffer_depth.uploadData(buffer);
                MemoryUtil.memFree(buffer);
        
            } else if (depth_image.format().channels != 1) {
                DepthMap8 depthMap = new DepthMap8(depth_image);
                ByteBuffer buffer = MemoryUtil.memAlloc(depthMap.size());
                buffer.put(depthMap.get()).flip();
                front_buffer_depth = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_depth.bindToActiveSlot();
                front_buffer_depth.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_depth.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_depth.clampToEdge();
                front_buffer_depth.uploadData(buffer);
                back_buffer_depth = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_depth.bindToActiveSlot();
                back_buffer_depth.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_depth.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_depth.clampToEdge();
                back_buffer_depth.uploadData(buffer);
                MemoryUtil.memFree(buffer);
                
            } else {
                front_buffer_depth = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_depth.bindToActiveSlot();
                front_buffer_depth.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_depth.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_depth.clampToEdge();
                front_buffer_depth.uploadData(depth_image.data());
                back_buffer_depth = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_depth.bindToActiveSlot();
                back_buffer_depth.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_depth.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_depth.clampToEdge();
                back_buffer_depth.uploadData(depth_image.data());
            }
            
            if (specular_image == null) {
                front_buffer_specular = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_specular.bindToActiveSlot();
                front_buffer_specular.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_specular.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_specular.clampToEdge();
                back_buffer_specular = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_specular.bindToActiveSlot();
                back_buffer_specular.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_specular.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_specular.clampToEdge();
                
            } else if (specular_image.format().channels != 1) {
                DepthMap8 depthMap = new DepthMap8(specular_image);
                ByteBuffer buffer = MemoryUtil.memAlloc(depthMap.size());
                buffer.put(depthMap.get()).flip();
                front_buffer_specular = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_specular.bindToActiveSlot();
                front_buffer_specular.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_specular.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_specular.clampToEdge();
                front_buffer_specular.uploadData(buffer);
                back_buffer_specular = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_specular.bindToActiveSlot();
                back_buffer_specular.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_specular.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_specular.clampToEdge();
                back_buffer_specular.uploadData(buffer);
                MemoryUtil.memFree(buffer);
                
            } else {
                front_buffer_specular = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_specular.bindToActiveSlot();
                front_buffer_specular.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_specular.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_specular.clampToEdge();
                front_buffer_specular.uploadData(specular_image.data());
                back_buffer_specular = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_specular.bindToActiveSlot();
                back_buffer_specular.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_specular.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_specular.clampToEdge();
                back_buffer_specular.uploadData(specular_image.data());
            }
            
            if (emissive_image == null) {
                front_buffer_emissive = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_emissive.bindToActiveSlot();
                front_buffer_emissive.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_emissive.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_emissive.clampToEdge();
                back_buffer_emissive = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_emissive.bindToActiveSlot();
                back_buffer_emissive.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_emissive.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_emissive.clampToEdge();
                
            } else if (emissive_image.format().channels != 1) {
                DepthMap8 depthMap = new DepthMap8(emissive_image);
                ByteBuffer buffer = MemoryUtil.memAlloc(depthMap.size());
                buffer.put(depthMap.get()).flip();
                front_buffer_emissive = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_emissive.bindToActiveSlot();
                front_buffer_emissive.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_emissive.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_emissive.clampToEdge();
                front_buffer_emissive.uploadData(buffer);
                back_buffer_emissive = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_emissive.bindToActiveSlot();
                back_buffer_emissive.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_emissive.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_emissive.clampToEdge();
                back_buffer_emissive.uploadData(buffer);
                MemoryUtil.memFree(buffer);
                
            } else {
                front_buffer_emissive = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_emissive.bindToActiveSlot();
                front_buffer_emissive.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_emissive.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_emissive.clampToEdge();
                front_buffer_emissive.uploadData(emissive_image.data());
                back_buffer_emissive = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_emissive.bindToActiveSlot();
                back_buffer_emissive.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_emissive.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_emissive.clampToEdge();
                back_buffer_emissive.uploadData(emissive_image.data());
            }
            
            return new Textures(
                    color_source,
                    front_buffer_depth,
                    front_buffer_specular,
                    front_buffer_emissive,
                    back_buffer_depth,
                    back_buffer_specular,
                    back_buffer_emissive);
            
        } return null;
    }
    
    public Image diffuse_image() {
        return diffuse_image;
    }
    
    public Image depth_image() {
        return depth_image;
    }
    
    public Image specular_image() {
        return specular_image;
    }
    
    public Image emissive_image() {
        return emissive_image;
    }
    
    public Status status() {
        return status;
    }
    
    public String name() {
        return name;
    }
    
    /**
     * The PngImporter is reusable after disposal.
     */
    public void dispose() {
        Disposable.dispose(diffuse_image,depth_image,specular_image,emissive_image);
        diffuse_image = null;
        depth_image = null;
        specular_image = null;
        emissive_image = null;
        status = validate();
    }
    
    private String resolveName(Path diffusePath) {
        String filename = diffusePath.getFileName().toString();
        return filename.replace(".png","");
    }
}
