package io.github.heathensoft.canvas.f.io;

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
import static io.github.heathensoft.canvas.f.ENUM.*;


/**
 * The PngImporter is reusable after disposal (unlike most other disposables)
 * Png files only
 * @author Frederik Dahl
 * 17/01/2023
 */


public class PngImporter implements Disposable {
    
    private Image details_image;
    private Image volume_image;
    private Image diffuse_image;
    private Image specular_image;
    private Image emissive_image;
    private ImportStatus status;
    private Path import_path;
    private String name;
    
    public record Textures(String name, Path directory,
                           Texture color_source,
                           Texture front_buffer_details,
                           Texture front_buffer_volume,
                           Texture front_buffer_specular,
                           Texture front_buffer_emissive,
                           Texture back_buffer_details,
                           Texture back_buffer_volume,
                           Texture back_buffer_specular,
                           Texture back_buffer_emissive) { }
    
    
    public PngImporter() {
        status = ImportStatus.INCOMPLETE;
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
        import_path = path.getParent();
    }
    
    public void importDetailsImage(Path path) throws Exception {
        ByteBuffer buffer = new Resources().toBufferExternal(path);
        Image image = new Image(buffer,true);
        MemoryUtil.memFree(buffer);
        if (details_image != null) {
            details_image.dispose();
        } details_image = image;
        status = validate();
    }
    
    public void importVolumeImage(Path path) throws Exception {
        ByteBuffer buffer = new Resources().toBufferExternal(path);
        Image image = new Image(buffer,true);
        MemoryUtil.memFree(buffer);
        if (volume_image != null) {
            volume_image.dispose();
        } volume_image = image;
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
    
    private ImportStatus validate() {
        if (diffuse_image == null) {
            return ImportStatus.INCOMPLETE;
        } int num_loaded = 1;
        int width = diffuse_image.width();
        int height = diffuse_image.height();
        if (details_image != null) {
            if (details_image.width() == width && details_image.height() == height) {
                num_loaded++;
            } else return ImportStatus.SIZES_NOT_MATCHING;
        } if (volume_image != null) {
            if (volume_image.width() == width && volume_image.height() == height) {
                num_loaded++;
            } else return ImportStatus.SIZES_NOT_MATCHING;
        } if (specular_image != null) {
            if (specular_image.width() == width && specular_image.height() == height) {
                num_loaded++;
            } else return ImportStatus.SIZES_NOT_MATCHING;
        } if (emissive_image != null) {
            if (emissive_image.width() == width && emissive_image.height() == height) {
                num_loaded++;
            } else return ImportStatus.SIZES_NOT_MATCHING;
        } if (num_loaded == 5) return ImportStatus.COMPLETE_READY;
        return ImportStatus.INCOMPLETE_READY;
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
            Texture front_buffer_details;
            Texture front_buffer_volume;
            Texture front_buffer_specular;
            Texture front_buffer_emissive;
            Texture back_buffer_details;
            Texture back_buffer_volume;
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
            
            
            if (details_image == null) {
                DepthMap8 depthMap = new DepthMap8(diffuse_image);
                ByteBuffer buffer = MemoryUtil.memAlloc(depthMap.size());
                buffer.put(depthMap.get()).flip();
                front_buffer_details = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_details.bindToActiveSlot();
                front_buffer_details.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_details.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_details.clampToEdge();
                front_buffer_details.uploadData(buffer);
                back_buffer_details = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_details.bindToActiveSlot();
                back_buffer_details.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_details.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_details.clampToEdge();
                back_buffer_details.uploadData(buffer);
                MemoryUtil.memFree(buffer);
        
            } else if (details_image.format().channels != 1) {
                DepthMap8 depthMap = new DepthMap8(details_image);
                ByteBuffer buffer = MemoryUtil.memAlloc(depthMap.size());
                buffer.put(depthMap.get()).flip();
                front_buffer_details = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_details.bindToActiveSlot();
                front_buffer_details.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_details.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_details.clampToEdge();
                front_buffer_details.uploadData(buffer);
                back_buffer_details = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_details.bindToActiveSlot();
                back_buffer_details.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_details.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_details.clampToEdge();
                back_buffer_details.uploadData(buffer);
                MemoryUtil.memFree(buffer);
                
            } else {
                front_buffer_details = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_details.bindToActiveSlot();
                front_buffer_details.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_details.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_details.clampToEdge();
                front_buffer_details.uploadData(details_image.data());
                back_buffer_details = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_details.bindToActiveSlot();
                back_buffer_details.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_details.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_details.clampToEdge();
                back_buffer_details.uploadData(details_image.data());
            }
    
            if (volume_image == null) {
                byte color = 0x7F;
                int size = WIDTH * HEIGHT;
                ByteBuffer buffer = MemoryUtil.memAlloc(size);
                for (int i = 0; i < size; i++) buffer.put(color);
                front_buffer_volume = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_volume.bindToActiveSlot();
                front_buffer_volume.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_volume.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_volume.clampToEdge();
                front_buffer_volume.uploadData(buffer.flip());
                back_buffer_volume = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_volume.bindToActiveSlot();
                back_buffer_volume.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_volume.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_volume.clampToEdge();
                back_buffer_volume.uploadData(buffer);
                MemoryUtil.memFree(buffer);
        
            } else if (volume_image.format().channels != 1) {
                DepthMap8 depthMap = new DepthMap8(volume_image);
                ByteBuffer buffer = MemoryUtil.memAlloc(depthMap.size());
                buffer.put(depthMap.get()).flip();
                front_buffer_volume = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_volume.bindToActiveSlot();
                front_buffer_volume.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_volume.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_volume.clampToEdge();
                front_buffer_volume.uploadData(buffer);
                back_buffer_volume = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_volume.bindToActiveSlot();
                back_buffer_volume.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_volume.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_volume.clampToEdge();
                back_buffer_volume.uploadData(buffer);
                MemoryUtil.memFree(buffer);
        
            } else {
                front_buffer_volume = Texture.generate2D(WIDTH,HEIGHT);
                front_buffer_volume.bindToActiveSlot();
                front_buffer_volume.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                front_buffer_volume.filter(GL_NEAREST,GL_NEAREST);
                front_buffer_volume.clampToEdge();
                front_buffer_volume.uploadData(volume_image.data());
                back_buffer_volume = Texture.generate2D(WIDTH,HEIGHT);
                back_buffer_volume.bindToActiveSlot();
                back_buffer_volume.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
                back_buffer_volume.filter(GL_NEAREST,GL_NEAREST);
                back_buffer_volume.clampToEdge();
                back_buffer_volume.uploadData(volume_image.data());
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
                    name,
                    import_path,
                    color_source,
                    front_buffer_details,
                    front_buffer_volume,
                    front_buffer_specular,
                    front_buffer_emissive,
                    back_buffer_details,
                    back_buffer_volume,
                    back_buffer_specular,
                    back_buffer_emissive);
            
        } return null;
    }
    
    public Image diffuse_image() {
        return diffuse_image;
    }
    
    public Image details_image() {
        return details_image;
    }
    
    public Image volume_image() {
        return volume_image;
    }
    
    public Image specular_image() {
        return specular_image;
    }
    
    public Image emissive_image() {
        return emissive_image;
    }
    
    public ImportStatus status() {
        return status;
    }
    
    public String name() {
        return name;
    }
    
    /**
     * The PngImporter is reusable after disposal.
     */
    public void dispose() {
        Disposable.dispose(
                diffuse_image,
                details_image,
                volume_image,
                specular_image,
                emissive_image);
        diffuse_image = null;
        details_image = null;
        volume_image = null;
        specular_image = null;
        emissive_image = null;
        status = validate();
        name = "untitled";
    }
    
    private String resolveName(Path diffusePath) {
        String filename = diffusePath.getFileName().toString();
        return filename.replace(".png","");
    }
    
    
}
