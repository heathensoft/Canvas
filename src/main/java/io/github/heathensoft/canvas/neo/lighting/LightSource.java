package io.github.heathensoft.canvas.neo.lighting;

import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import org.joml.Math;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static io.github.heathensoft.canvas.neo.lighting.Attenuation.*;
import static io.github.heathensoft.canvas.neo.DefaultSettings.*;

/**
 * @author Frederik Dahl
 * 22/02/2023
 */


public class LightSource {
    
    
    public static final Attenuation[] PRESETS = new Attenuation[] {ATT_100,ATT_160,ATT_200,ATT_325,ATT_600,ATT_3250};
    public Attenuation attenuation;
    public Vector3f position;
    public Color color;
    public float diffuse_strength;
    public float ambient_strength;
    public boolean point_light;
    private int current_preset;
    
    public LightSource() {
        current_preset = Math.min(PRESETS.length - 1, Math.max(0, LIGHTING_SOURCE_ATTENUATION_PRESET));
        attenuation = new Attenuation(PRESETS[current_preset]);
        color = new Color(LIGHTING_SOURCE_COLOR);
        position = new Vector3f(LIGHTING_SOURCE_POSITION);
        diffuse_strength = LIGHTING_SOURCE_DIFFUSE_STRENGTH;
        ambient_strength = LIGHTING_SOURCE_AMBIENT_STRENGTH;
        point_light = LIGHTING_SOURCE_POINT_LIGHT;
    }
    
    public void togglePointLight() {
        point_light = !point_light;
    }
    
    public void increaseAttenuation() {
        Attenuation preset = PRESETS[current_preset];
        if (attenuation.equals(preset)) {
            int index = Math.min(current_preset + 1, PRESETS.length - 1);
            if (index > current_preset) {
                current_preset = index;
                preset = PRESETS[current_preset];
                attenuation.set(preset);
            }
        } else {
            attenuation.set(preset);
        }
    }
    
    public void decreaseAttenuation() {
        Attenuation preset = PRESETS[current_preset];
        if (attenuation.equals(preset)) {
            int index = Math.max(current_preset - 1, 0);
            if (index < current_preset) {
                current_preset = index;
                preset = PRESETS[current_preset];
                attenuation.set(preset);
            }
        } else {
            attenuation.set(preset);
        }
    }
    
    public FloatBuffer get(FloatBuffer buffer) {
        attenuation.get(buffer);
        color.getRGB(buffer);
        buffer.put(point_light ? 1.0f : 0.0f);
        BufferObject.put(position,buffer);
        buffer.put(diffuse_strength);
        BufferObject.putPadding(3,buffer);
        buffer.put(ambient_strength);
        return buffer;
    }
    
    /* 16
    Struct LightSource {
    Attenuation attenuation;
    vec3 color;
    float point_light;
    vec3 position;
    float diffuse_strength;
    vec3 std40_padding;
    float ambient_strength;
    }
     */
    
    
    
}
