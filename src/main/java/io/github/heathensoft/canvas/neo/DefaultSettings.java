package io.github.heathensoft.canvas.neo;

import io.github.heathensoft.canvas.neo.lighting.Attenuation;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import org.joml.Vector3f;


/**
 * @author Frederik Dahl
 * 20/02/2023
 */


public class DefaultSettings {
    
    public static Color LIGHTING_SOURCE_COLOR = new Color(0.9f,0.9f,0.9f,1.0f);
    public static Vector3f LIGHTING_SOURCE_POSITION = new Vector3f(0.0f,0.0f,40.0f);
    public static float LIGHTING_SOURCE_DIFFUSE_STRENGTH = 0.6f;
    public static float LIGHTING_SOURCE_AMBIENT_STRENGTH = 0.3f;
    public static int LIGHTING_SOURCE_ATTENUATION_PRESET = 4;
    public static boolean LIGHTING_SOURCE_POINT_LIGHT = false;
    
    public static Vector3f LIGHTING_AMBIENT_DIRECTION_BIAS = new Vector3f(0.0f,0.0f,1.0f);
    public static Color LIGHTING_AMBIENT_COLOR = new Color(0.0f,0.0f,1.0f,1.0f);
    public static float LIGHTING_AMBIENT_INFLUENCE = 0.5f;
    public static float LIGHTING_AMBIENT_INTENSITY = 0.25f;
    public static float LIGHTING_AMBIENT_LIGHT_WRAP = 0.25f;
    
    public static boolean LIGHTING_ON = true;
    public static boolean LIGHTING_SHADOWS = true;
    public static boolean LIGHTING_AMBIENT_OCCLUSION = true;
    public static boolean LIGHTING_BLOOM = true;
    public static boolean LIGHTING_PALETTE = false;
    public static boolean LIGHTING_RENDER_WHITE = false;
    public static float LIGHTING_GREYSCALE_MIXING = 1 / 16f;
    public static float LIGHTING_DIRECTIONAL_LIGHT_WRAP = 0.25f;
    public static float LIGHTING_SHADOW_STRENGTH = 1.0f;
    public static float LIGHTING_DEPTH_AMPLITUDE = 16f;
    public static float LIGHTING_BLOOM_THRESHOLD = 1.0f;
    public static float LIGHTING_GAMMA = 2.2f;
    
    
    
    
}
