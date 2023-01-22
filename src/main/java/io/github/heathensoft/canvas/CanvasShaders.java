package io.github.heathensoft.canvas;

/**
 * @author Frederik Dahl
 * 21/01/2023
 */


public class CanvasShaders {
    
    public static final String CANVAS_TO_SCREEN_SPACE_VERT = "res/glsl/canvas_to_screen_space.vert";
    public static final String CANVAS_TO_SCREEN_SPACE_FRAG = "res/glsl/canvas_to_screen_space.frag";
    public static final String TEXTURE_TO_CANVAS_SPACE_VERT = "res/glsl/texture_to_canvas_space.vert";
    public static final String TEXTURE_TO_CANVAS_SPACE_FRAG = "res/glsl/texture_to_canvas_space.frag";
    public static final String TEXTURE_PASSTHROUGH_VERT = "res/glsl/texture_passthrough.vert";
    public static final String TEXTURE_PASSTHROUGH_FRAG = "res/glsl/texture_passthrough.frag";
    public static final String CANVAS_BACKGROUND_VERT = "res/glsl/canvas_background.vert";
    public static final String CANVAS_BACKGROUND_FRAG = "res/glsl/canvas_background.frag";
    public static final String NORMAL_MAPPING_VERT = "res/glsl/normal_mapping.vert";
    public static final String NORMAL_MAPPING_FRAG = "res/glsl/normal_mapping.frag";
    
    
    
    public static final String U_SAMPLER = "u_sampler";
    public static final String U_SAMPLER_ARRAY = "u_sampler_array";
    public static final String U_AMPLITUDE = "u_amplitude";
}
