package io.github.heathensoft.canvas.f.io;

import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.surface.DepthMap8;
import io.github.heathensoft.jlib.lwjgl.graphics.surface.NormalMap;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 *
 * Later I would like to have alternatives for various packed formats.
 * In the meantime normals are 3 bytes and every format its own separate png file.
 *
 * @author Frederik Dahl
 * 18/01/2023
 */


public class PngExporter {
    
    private static final String extension = ".png";
    private static final String color_suffix = "_color";
    private static final String depth_suffix = "_depth";
    private static final String volume_suffix = "_volume";
    private static final String details_suffix = "_details";
    private static final String specular_suffix = "_specular";
    private static final String emissive_suffix = "_emissive";
    private static final String normals_suffix = "_normals";
    private static final String preview_suffix = "_preview";
    
    private String outputName;
    private Path outputDirectory;
    
    public PngExporter(Path outputDirectory, String outputName) {
        this.outputDirectory = outputDirectory;
        this.outputName = outputName;
        validateName();
    }
    
    public void exportColor(Texture diffuseTexture, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName = outputName + color_suffix;
        External color_out = new External(projectFolder.path().resolve(rootName + extension));
        if (!overwrite) {
            int numerator = 1;
            while (color_out.exist()) {
                String filename = rootName + "_" + numerator;
                color_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = diffuseTexture.width();
        int height = diffuseTexture.height();
        int channels = diffuseTexture.format().channels;
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * channels);
        diffuseTexture.bindToActiveSlot();
        diffuseTexture.get(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(color_out.path().toString(),width,height,
        channels,pixels,width * channels);
        MemoryUtil.memFree(pixels);
    }
    
    public void exportNormals(Texture normalsTexture, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName = outputName + normals_suffix;
        External color_out = new External(projectFolder.path().resolve(rootName + extension));
        if (!overwrite) {
            int numerator = 1;
            while (color_out.exist()) {
                String filename = rootName + "_" + numerator;
                color_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = normalsTexture.width();
        int height = normalsTexture.height();
        int channels = normalsTexture.format().channels;
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * channels);
        normalsTexture.bindToActiveSlot();
        normalsTexture.get(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(color_out.path().toString(),width,height,
                channels,pixels,width * channels);
        MemoryUtil.memFree(pixels);
    }
    
    public void exportDepth(Texture depthTexture, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName = outputName + depth_suffix;
        External color_out = new External(projectFolder.path().resolve(rootName + extension));
        if (!overwrite) {
            int numerator = 1;
            while (color_out.exist()) {
                String filename = rootName + "_" + numerator;
                color_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = depthTexture.width();
        int height = depthTexture.height();
        int channels = depthTexture.format().channels;
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * channels);
        depthTexture.bindToActiveSlot();
        depthTexture.get(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(color_out.path().toString(),width,height,
                channels,pixels,width * channels);
        MemoryUtil.memFree(pixels);
    }
    
    public void exportDetails(Texture depthTexture, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName = outputName + details_suffix;
        External color_out = new External(projectFolder.path().resolve(rootName + extension));
        if (!overwrite) {
            int numerator = 1;
            while (color_out.exist()) {
                String filename = rootName + "_" + numerator;
                color_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = depthTexture.width();
        int height = depthTexture.height();
        int channels = depthTexture.format().channels;
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * channels);
        depthTexture.bindToActiveSlot();
        depthTexture.get(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(color_out.path().toString(),width,height,
                channels,pixels,width * channels);
        MemoryUtil.memFree(pixels);
    }
    
    public void exportVolume(Texture depthTexture, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName = outputName + volume_suffix;
        External color_out = new External(projectFolder.path().resolve(rootName + extension));
        if (!overwrite) {
            int numerator = 1;
            while (color_out.exist()) {
                String filename = rootName + "_" + numerator;
                color_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = depthTexture.width();
        int height = depthTexture.height();
        int channels = depthTexture.format().channels;
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * channels);
        depthTexture.bindToActiveSlot();
        depthTexture.get(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(color_out.path().toString(),width,height,
                channels,pixels,width * channels);
        MemoryUtil.memFree(pixels);
    }
    
    @Deprecated
    public void exportDepthAndNormals(Texture depthTexture, float amplitude, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName_depth = outputName + depth_suffix;
        String rootName_normals = outputName + normals_suffix;
        External depth_out = new External(projectFolder.path().resolve(rootName_depth + extension));
        External normals_out = new External(projectFolder.path().resolve(rootName_normals + extension));
        if (!overwrite) {
            int numerator = 1;
            while (depth_out.exist()) {
                String filename = rootName_depth + "_" + numerator;
                depth_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            } numerator = 1;
            while (normals_out.exist()) {
                String filename = rootName_normals + "_" + numerator;
                normals_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = depthTexture.width();
        int height = depthTexture.height();
        int channels = depthTexture.format().channels;
        int size_needed = Math.max(channels,3); // reusing buffer for normals
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * size_needed);
        // Depth
        depthTexture.bindToActiveSlot();
        depthTexture.get(pixels);
        DepthMap8 depthMap = new DepthMap8(width,height,channels,pixels);
        pixels.clear().put(depthMap.get()).flip();
        stbi_flip_vertically_on_write(true);
        stbi_write_png(depth_out.path().toString(),width,height,
        1,pixels,width);
        // Normals
        NormalMap normalMap = new NormalMap(depthMap,amplitude);
        pixels.clear().put(normalMap.get()).flip();
        stbi_write_png(normals_out.path().toString(),width,height,
        3,pixels,width * 3);
        MemoryUtil.memFree(pixels);
    }
    
    public void exportSpecular(Texture specularTexture, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName = outputName + specular_suffix;
        External specular_out = new External(projectFolder.path().resolve(rootName + extension));
        if (!overwrite) {
            int numerator = 1;
            while (specular_out.exist()) {
                String filename = rootName + "_" + numerator;
                specular_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = specularTexture.width();
        int height = specularTexture.height();
        int channels = specularTexture.format().channels;
        
        // check if channels > 1
        
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * channels);
        specularTexture.bindToActiveSlot();
        specularTexture.get(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(specular_out.path().toString(),width,height,
        channels,pixels,width * channels);
        MemoryUtil.memFree(pixels);
    }
    
    public void exportEmissive(Texture emissiveTexture, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName = outputName + emissive_suffix;
        External emissive_out = new External(projectFolder.path().resolve(rootName + extension));
        if (!overwrite) {
            int numerator = 1;
            while (emissive_out.exist()) {
                String filename = rootName + "_" + numerator;
                emissive_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = emissiveTexture.width();
        int height = emissiveTexture.height();
        int channels = emissiveTexture.format().channels;
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * channels);
        emissiveTexture.bindToActiveSlot();
        emissiveTexture.get(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(emissive_out.path().toString(),width,height,
        channels,pixels,width * channels);
        MemoryUtil.memFree(pixels);
    }
    
    public void exportPreview(Texture previewTexture, boolean overwrite) throws Exception {
        External projectFolder = new External(outputDirectory);
        projectFolder.createDirectories();
        String rootName = outputName + preview_suffix;
        External preview_out = new External(projectFolder.path().resolve(rootName + extension));
        if (!overwrite) {
            int numerator = 1;
            while (preview_out.exist()) {
                String filename = rootName + "_" + numerator;
                preview_out.set(projectFolder.path().resolve(filename + extension));
                numerator++;
            }
        }
        int width = previewTexture.width();
        int height = previewTexture.height();
        int channels = previewTexture.format().channels;
        ByteBuffer pixels = MemoryUtil.memAlloc(width * height * channels);
        previewTexture.bindToActiveSlot();
        previewTexture.get(pixels);
        stbi_flip_vertically_on_write(true);
        stbi_write_png(preview_out.path().toString(),width,height,
        channels,pixels,width * channels);
        MemoryUtil.memFree(pixels);
    }
    
    
    public String outputName() {
        return outputName;
    }
    
    public void setOutputName(String outputName) {
        this.outputName = outputName;
        validateName();
    }
    
    public Path outputDirectory() {
        return outputDirectory;
    }
    
    public void setOutputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    private void validateName() {
        if (outputName == null) {
            outputName = "untitled";
        } else {
            outputName = outputName.replace(" ","");
            outputName = outputName.length() == 0 ? "untitled" : outputName;
        }
    }
}
