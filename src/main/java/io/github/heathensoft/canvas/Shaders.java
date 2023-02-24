package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.joml.Vector3f;
import org.tinylog.Logger;

/**
 * @author Frederik Dahl
 * 21/01/2023
 */


public class Shaders {
    
    public static final String AREA_TO_BRUSH_OVERLAY_VERT = "res/glsl/editor/brush/area_to_brush_overlay.vert";
    public static final String AREA_TO_BRUSH_OVERLAY_FRAG = "res/glsl/editor/brush/area_to_brush_overlay.frag";
    public static final String STROKE_TO_BRUSH_OVERLAY_VERT = "res/glsl/editor/brush/stroke_to_brush_overlay.vert";
    public static final String STROKE_TO_BRUSH_OVERLAY_GEOM = "res/glsl/editor/brush/stroke_to_brush_overlay.geom";
    public static final String STROKE_TO_BRUSH_OVERLAY_FRAG = "res/glsl/editor/brush/stroke_to_brush_overlay.frag";
    
    public static final String BACK_TO_FRONTBUFFER_VERT = "res/glsl/editor/texture/back_to_frontbuffer.vert";
    public static final String BACK_TO_FRONTBUFFER_FRAG = "res/glsl/editor/texture/back_to_frontbuffer.frag";
    public static final String FRONT_TO_BACKBUFFER_VERT = "res/glsl/editor/texture/front_to_backbuffer.vert";
    public static final String FRONT_TO_BACKBUFFER_FRAG = "res/glsl/editor/texture/front_to_backbuffer.frag";
    public static final String TEXTURE_DEPTH_MIXING_VERT = "res/glsl/editor/texture/texture_depth_mixing.vert";
    public static final String TEXTURE_DEPTH_MIXING_FRAG = "res/glsl/editor/texture/texture_depth_mixing.frag";
    public static final String TEXTURE_LIGHTING_VERT = "res/glsl/editor/texture/texture_lighting.vert";
    public static final String TEXTURE_LIGHTING_FRAG = "res/glsl/editor/texture/texture_lighting.frag";
    public static final String TEXTURE_SMOOTHEN_VERT = "res/glsl/editor/texture/texture_smoothen.vert";
    public static final String TEXTURE_SMOOTHEN_FRAG = "res/glsl/editor/texture/texture_smoothen.frag";
    public static final String TEXTURE_NORMAL_MAPPING_VERT = "res/glsl/editor/texture/texture_normal_mapping.vert";
    public static final String TEXTURE_NORMAL_MAPPING_FRAG = "res/glsl/editor/texture/texture_normal_mapping.frag";
    public static final String TEXTURE_PASSTHROUGH_VERT = "res/glsl/editor/texture/texture_passthrough.vert";
    public static final String TEXTURE_PASSTHROUGH_FRAG = "res/glsl/editor/texture/texture_passthrough.frag";
    public static final String TEXTURE_SHADOW_MAPPING_VERT = "res/glsl/editor/texture/texture_shadow_mapping.vert";
    public static final String TEXTURE_SHADOW_MAPPING_FRAG = "res/glsl/editor/texture/texture_shadow_mapping.frag";
    public static final String TEXTURE_AMBIENT_OCCLUSION_VERT = "res/glsl/editor/texture/texture_ambient_occlusion.vert";
    public static final String TEXTURE_AMBIENT_OCCLUSION_FRAG = "res/glsl/editor/texture/texture_ambient_occlusion.frag";
    
    public static final String CANVAS_BACKGROUND_VERT = "res/glsl/editor/canvas_background.vert";
    public static final String CANVAS_BACKGROUND_FRAG = "res/glsl/editor/canvas_background.frag";
    public static final String CANVAS_TO_SCREEN_SPACE_VERT = "res/glsl/editor/canvas_to_screen_space.vert";
    public static final String CANVAS_TO_SCREEN_SPACE_FRAG = "res/glsl/editor/canvas_to_screen_space.frag";
    public static final String TEXTURE_TO_CANVAS_SPACE_VERT = "res/glsl/editor/texture_to_canvas_space.vert";
    public static final String TEXTURE_TO_CANVAS_SPACE_FRAG = "res/glsl/editor/texture_to_canvas_space.frag";
    
    
    public static final String U_SAMPLER_2D = "u_sampler_2d";               // int
    public static final String U_SAMPLER_3D = "u_sampler_3d";               // int
    public static final String U_SAMPLER_ARRAY = "u_sampler_array";         // int[]
    public static final String U_DRAG_AREA = "u_drag_area";                 // vec4
    public static final String U_CANVAS_SIZE_INV = "u_canvas_size_inv";     // vec2
    public static final String U_OCCLUSION_SAMPLES = "u_occlusion_samples"; // vec3[]
    
    private static boolean initialized;
    
    public static ShaderProgram texturePassthroughProgram;
    public static ShaderProgram textureSmoothenProgram;
    public static ShaderProgram textureLightingProgram;
    public static ShaderProgram textureShadowsProgram;
    public static ShaderProgram textureNormalsProgram;
    public static ShaderProgram textureDepthMixingProgram;
    public static ShaderProgram textureAmbientOcclusionProgram;
    public static ShaderProgram textureToCanvasProgram;
    public static ShaderProgram canvasBackgroundProgram;
    public static ShaderProgram canvasToScreenProgram;
    public static ShaderProgram strokeToBrushProgram;
    public static ShaderProgram areaToBrushProgram;
    public static ShaderProgram frontToBackBufferProgram;
    public static ShaderProgram backToFrontBufferProgram;
    
    
    public static void initialize() throws Exception {
        if (!initialized) {
            Logger.info("initializing shaders");
            Resources io = new Resources(Shaders.class);
    
            textureSmoothenProgram= new ShaderProgram(
                    io.asString(TEXTURE_SMOOTHEN_VERT),
                    io.asString(TEXTURE_SMOOTHEN_FRAG));
            textureSmoothenProgram.createUniform(U_SAMPLER_2D);
            
            texturePassthroughProgram = new ShaderProgram(
                    io.asString(TEXTURE_PASSTHROUGH_VERT),
                    io.asString(TEXTURE_PASSTHROUGH_FRAG));
            texturePassthroughProgram.createUniform(U_SAMPLER_2D);
            
            textureLightingProgram = new ShaderProgram(
                    io.asString(TEXTURE_LIGHTING_VERT),
                    io.asString(TEXTURE_LIGHTING_FRAG));
            textureLightingProgram.createUniform(U_SAMPLER_3D);
            textureLightingProgram.createUniform(U_SAMPLER_ARRAY);
            
            textureShadowsProgram = new ShaderProgram(
                    io.asString(TEXTURE_SHADOW_MAPPING_VERT),
                    io.asString(TEXTURE_SHADOW_MAPPING_FRAG));
            textureShadowsProgram.createUniform(U_SAMPLER_2D);
            
            textureNormalsProgram = new ShaderProgram(
                    io.asString(TEXTURE_NORMAL_MAPPING_VERT),
                    io.asString(TEXTURE_NORMAL_MAPPING_FRAG));
            textureNormalsProgram.createUniform(U_SAMPLER_ARRAY);
            
            textureAmbientOcclusionProgram = new ShaderProgram(
                    io.asString(TEXTURE_AMBIENT_OCCLUSION_VERT),
                    io.asString(TEXTURE_AMBIENT_OCCLUSION_FRAG));
            textureAmbientOcclusionProgram.createUniform(U_SAMPLER_ARRAY);
            textureAmbientOcclusionProgram.createUniform(U_OCCLUSION_SAMPLES);
            textureAmbientOcclusionProgram.use();
            uploadOcclusionSamples();
            ShaderProgram.useZERO();
            
            textureDepthMixingProgram = new ShaderProgram(
                    io.asString(TEXTURE_DEPTH_MIXING_VERT),
                    io.asString(TEXTURE_DEPTH_MIXING_FRAG));
            textureDepthMixingProgram.createUniform(U_SAMPLER_ARRAY);
            
            textureToCanvasProgram = new ShaderProgram(
                    io.asString(TEXTURE_TO_CANVAS_SPACE_VERT),
                    io.asString(TEXTURE_TO_CANVAS_SPACE_FRAG));
            textureToCanvasProgram.createUniform(U_SAMPLER_ARRAY);
            
            canvasBackgroundProgram = new ShaderProgram(
                    io.asString(CANVAS_BACKGROUND_VERT),
                    io.asString(CANVAS_BACKGROUND_FRAG));
            canvasBackgroundProgram.createUniform(U_SAMPLER_2D);
            
            canvasToScreenProgram = new ShaderProgram(
                    io.asString(CANVAS_TO_SCREEN_SPACE_VERT),
                    io.asString(CANVAS_TO_SCREEN_SPACE_FRAG));
            canvasToScreenProgram.createUniform(U_SAMPLER_ARRAY);
            canvasToScreenProgram.createUniform(U_CANVAS_SIZE_INV);
            
            strokeToBrushProgram = new ShaderProgram(
                    io.asString(STROKE_TO_BRUSH_OVERLAY_VERT),
                    io.asString(STROKE_TO_BRUSH_OVERLAY_GEOM),
                    io.asString(STROKE_TO_BRUSH_OVERLAY_FRAG));
            strokeToBrushProgram.createUniform(U_SAMPLER_ARRAY);
    
            areaToBrushProgram = new ShaderProgram(
                    io.asString(AREA_TO_BRUSH_OVERLAY_VERT),
                    io.asString(AREA_TO_BRUSH_OVERLAY_FRAG));
            areaToBrushProgram.createUniform(U_DRAG_AREA);
            areaToBrushProgram.createUniform(U_SAMPLER_2D);
            
             frontToBackBufferProgram = new ShaderProgram(
                    io.asString(FRONT_TO_BACKBUFFER_VERT),
                    io.asString(FRONT_TO_BACKBUFFER_FRAG));
            frontToBackBufferProgram.createUniform(U_SAMPLER_2D);
            
            backToFrontBufferProgram = new ShaderProgram(
                    io.asString(BACK_TO_FRONTBUFFER_VERT),
                    io.asString(BACK_TO_FRONTBUFFER_FRAG));
            backToFrontBufferProgram.createUniform(U_SAMPLER_ARRAY);
            
            
            Logger.info("shaders initialized");
            initialized = true;
        }
    }
    
    private static void uploadOcclusionSamples() {
        final int KERNEL_SIZE = 128;
        Vector3f[] samples = new Vector3f[KERNEL_SIZE];
        for (int i = 0; i < KERNEL_SIZE; i++) {
            float scale = (float)i / (float)KERNEL_SIZE;
            scale = 0.1f + scale * scale * 0.9f;
            float x = (float) (Math.random() * 2.0 - 1.0);
            float y = (float) (Math.random() * 2.0 - 1.0);
            float z = (float) (Math.random() * scale);
            samples[i] = new Vector3f(x,y,z);
        } textureAmbientOcclusionProgram.setUniform(U_OCCLUSION_SAMPLES,samples);
    }
    
    public static void dispose() {
        if (initialized) {
            Logger.info("disposing shaders");
            Disposable.dispose(
                    texturePassthroughProgram,
                    textureLightingProgram,
                    textureShadowsProgram,
                    textureNormalsProgram,
                    textureDepthMixingProgram,
                    textureAmbientOcclusionProgram,
                    textureToCanvasProgram,
                    canvasBackgroundProgram,
                    canvasToScreenProgram,
                    strokeToBrushProgram,
                    areaToBrushProgram,
                    frontToBackBufferProgram,
                    backToFrontBufferProgram);
            Logger.info("shaders disposed");
            initialized = false;
        }
    }
}
