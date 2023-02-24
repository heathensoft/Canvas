package io.github.heathensoft.canvas.neo.lighting;

import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static io.github.heathensoft.canvas.neo.DefaultSettings.*;

/**
 * If you had multiple lights, you would have to blend this with each
 * before adding them together. Could theoretically have multiple of these
 * covering various areas. But would have to mix all ambient sources together
 * before calculating that new ambience with all light-sources.
 * If I wanted to simulate a day/night-cycle I could use this as the night
 * color. fluctuating the weight as a function of time and/or the sun's direction.
 * Also, fluctuating the color between east and west sun direction.
 *
 * @author Frederik Dahl
 * 21/02/2023
 */


public class Ambience {
    
    public Vector3f directional_bias; // this gets added with the UP vector and normalized;
    public Color color; // The color of the "hemispheric" light
    public float influence; // The influence of this over the light sources ambience
    public float intensity; // The intensity of the "hemispheric" light color
    public float light_wrap; // wrapping the light around surfaces (The directional bias would seem as strong)
    
    
    public Ambience() {
        directional_bias = new Vector3f(LIGHTING_AMBIENT_DIRECTION_BIAS);
        color = new Color(LIGHTING_AMBIENT_COLOR);
        influence = LIGHTING_AMBIENT_INFLUENCE;
        intensity = LIGHTING_AMBIENT_INTENSITY;
        light_wrap = LIGHTING_AMBIENT_LIGHT_WRAP;
    }
    
    public FloatBuffer get(FloatBuffer buffer) {
        color.getRGB(buffer);
        buffer.put(intensity);
        BufferObject.put(directional_bias,buffer);
        buffer.put(influence);
        BufferObject.putPadding(3,buffer);
        buffer.put(light_wrap);
        return buffer;
    }
    
    /* 12
    Struct Ambience {
    vec3 color;
    float intensity;
    vec3 directional_bias;
    float influence;
    vec3 std40_padding;
    float light_wrap;
    }
     */
    
    
    
}
