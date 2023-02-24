package io.github.heathensoft.canvas.neo;

import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import org.joml.Vector3f;

/**
 * @author Frederik Dahl
 * 21/02/2023
 */


public class Lighting {
    
    private Color light_color;
    private Color ambient_light_color;
    private Vector3f ambient_light_direction;
    private Vector3f light_position;
    
    private float light_diffuse;
    private float light_ambience;
    private float light_brightness;
    private float scene_ambience;
    
}
