package io.github.heathensoft.canvas.light;

import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

/**
 * @author Frederik Dahl
 * 23/01/2023
 */


public class PointLight {
    
    private final Color color;
    private final Vector3f position;
    private final Attenuation attenuation;
    private float diffuse;
    private float ambience;
    
    public PointLight(Vector3f position, Color color) {
        this(position,color,Attenuation.ATT_65,0.5f,0.5f);
    }
    
    public PointLight(Vector3f position, Color color, Attenuation attenuation, float diffuse, float ambience) {
        this.position = new Vector3f(position);
        this.color = new Color(color);
        this.attenuation = new Attenuation(attenuation);
        this.diffuse = diffuse;
        this.ambience = ambience;
    }
    
    public Color color() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color.set(color);
    }
    
    public Vector3f position() {
        return position;
    }
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
    }
    
    public Attenuation attenuation() {
        return attenuation;
    }
    
    public void setAttenuation(Attenuation attenuation) {
        this.attenuation.set(attenuation);
    }
    
    public float diffuseStrength() {
        return diffuse;
    }
    
    public void setDiffuseStrength(float diffuse) {
        this.diffuse = diffuse;
    }
    
    public float ambience() {
        return ambience;
    }
    
    public void setAmbience(float ambience) {
        this.ambience = ambience;
    }
    
    public FloatBuffer get(FloatBuffer buffer) {
        buffer.put(position.x).put(position.y).put(position.z).put(ambience);
        buffer.put(color.r).put(color.g).put(color.b).put(diffuse);
        attenuation.get(buffer);
        buffer.put(0.0f);
        return buffer;
    }
    
    public static int sizeFloat() {
        return 12;
    }
    
}
