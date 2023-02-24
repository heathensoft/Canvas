package io.github.heathensoft.canvas.neo.lighting;

import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;

import java.nio.FloatBuffer;

import static io.github.heathensoft.canvas.neo.DefaultSettings.*;

/**
 * @author Frederik Dahl
 * 24/02/2023
 */


public class Lighting {
    
    public Ambience ambience;
    public LightSource source;
    
    public boolean lighting;
    public boolean shadows;
    public boolean render_white;
    public boolean ambient_occlusion;
    public boolean bloom; // threshold
    public boolean palette;
    
    public float greyscale_mixing;          // 0 -> 1
    public float directional_light_wrap;    // 0 -> 1
    public float shadow_strength;           // 0 -> ...
    public float depth_amplitude;           // 1 -> ...
    public float bloom_threshold;           // 0 -> ...
    public float gamma;                     // 1 -> 2.4
    
    public Lighting() {
        ambience = new Ambience();
        source = new LightSource();
        lighting = LIGHTING_ON;
        shadows = LIGHTING_SHADOWS;
        render_white = LIGHTING_RENDER_WHITE;
        ambient_occlusion = LIGHTING_AMBIENT_OCCLUSION;
        bloom = LIGHTING_BLOOM;
        palette = LIGHTING_PALETTE;
        greyscale_mixing = LIGHTING_GREYSCALE_MIXING;
        directional_light_wrap = LIGHTING_DIRECTIONAL_LIGHT_WRAP;
        shadow_strength = LIGHTING_SHADOW_STRENGTH;
        depth_amplitude = LIGHTING_DEPTH_AMPLITUDE;
        bloom_threshold = LIGHTING_BLOOM_THRESHOLD;
        gamma = LIGHTING_GAMMA;
    }
    
    public FloatBuffer get(FloatBuffer buffer) {
        source.get(buffer);
        ambience.get(buffer);
        buffer.put(greyscale_mixing);
        buffer.put(directional_light_wrap);
        buffer.put(shadow_strength);
        buffer.put(depth_amplitude);
        buffer.put(bloom_threshold);
        buffer.put(gamma);
        buffer.put(conditionals());
        BufferObject.putPadding(1,buffer);
        return buffer;
    }
    
    /* 36
    Struct Lighting {
    LightSource source;
    Ambience ambience;
    float greyscale_mixing;
    float directional_light_wrap;
    float shadow_strength;
    float depth_amplitude;
    float bloom_threshold;
    float conditionals;
    float std140_padding;
   
    }
     */
    
    private int conditionals() {
        int conditionals = 0;
        conditionals |= ( lighting          ? 1  : 0);
        conditionals |= ( shadows           ? 2  : 0);
        conditionals |= ( render_white      ? 4  : 0);
        conditionals |= ( ambient_occlusion ? 8  : 0);
        conditionals |= ( bloom             ? 16 : 0);
        conditionals |= ( palette           ? 32 : 0);
        return conditionals;
    }
}
