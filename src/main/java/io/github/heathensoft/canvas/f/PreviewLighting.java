package io.github.heathensoft.canvas.f;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import org.joml.Math;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 03/02/2023
 */


public class PreviewLighting implements Disposable {
    
    // https://wiki.ogre3d.org/tiki-index.php?page=-Point+Light+Attenuation
    
    public record Attenuation(float constant, float linear, float quadratic) {
        public void get(FloatBuffer buffer) {
            buffer.put(constant).put(linear).put(quadratic).put(0);
        }
        public static int size() { return 4; }
    }
    public static final Attenuation ATT_100 = new Attenuation(1.0f,0.045f,0.0075f);
    public static final Attenuation ATT_160 = new Attenuation(1.0f,0.027f,0.0028f);
    public static final Attenuation ATT_200 = new Attenuation(1.0f,0.022f,0.0019f);
    public static final Attenuation ATT_325 = new Attenuation(1.0f,0.014f,0.0007f);
    public static final Attenuation ATT_600 = new Attenuation(1.0f,0.007f,0.0002f);
    public static final Attenuation ATT_3250 = new Attenuation(1.0f,0.0014f,0.000007f);
    public static final Attenuation[] PRESETS = new Attenuation[] {ATT_100,ATT_160,ATT_200,ATT_325,ATT_600,ATT_3250};
    
    
    private static PreviewLighting instance;
    private static final int BINDING_POINT = 4;
    private static final int ATT_BLOCK_SIZE = PRESETS.length * Attenuation.size();
    private static final int UNIFORM_BLOCK_SIZE = ATT_BLOCK_SIZE + 12;
    
    
    public static PreviewLighting get() {
        if (instance == null) {
            instance = new PreviewLighting();
        } return instance;
    }
    
    private static FloatBuffer getPresets(FloatBuffer buffer) {
        for (Attenuation preset : PRESETS) {
            preset.get(buffer);
        } return buffer;
    }
    
    private final BufferObject uniformBuffer;
    private final Vector3f position;
    private final Color color;
    private float diffuse;
    private float ambience;
    private float brightness;
    private float pointLight;
    
    private PreviewLighting() {
        position = new Vector3f();
        color = Color.WHITE.cpy();
        uniformBuffer = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW).bind();
        uniformBuffer.bufferData((long) UNIFORM_BLOCK_SIZE * Float.BYTES);
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(ATT_BLOCK_SIZE);
            uniformBuffer.bufferSubData(getPresets(buffer).flip(),0);
        } uniformBuffer.bindBufferBase(BINDING_POINT);
    }
    
    public void uploadUniformBlock() {
        uniformBuffer.bind();
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(UNIFORM_BLOCK_SIZE - ATT_BLOCK_SIZE);
            BufferObject.put(position,buffer);
            buffer.put(ambience);
            color.getRGB(buffer);
            buffer.put(diffuse);
            BufferObject.putPadding(2,buffer);
            buffer.put(brightness).put(pointLight);
            uniformBuffer.bufferSubData(buffer.flip(),ATT_BLOCK_SIZE);
        }
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
    
    public void setPosition(Vector2f position) {
        this.position.set(position.x,position.y,this.position.z);
    }
    
    public float diffuseStrength() {
        return diffuse;
    }
    
    public void setDiffuseStrength(float diffuse) {
        this.diffuse = Math.clamp(0.0f,1.0f,diffuse);
    }
    
    public float ambience() {
        return ambience;
    }
    
    public void setAmbience(float ambience) {
        this.ambience =  Math.clamp(0.0f,1.0f, ambience);
    }
    
    public void increaseBrightness() {
        int brightness = (int) this.brightness - 1;
        this.brightness = Math.clamp(0,PRESETS.length-1,brightness);
    }
    
    public void decreaseBrightness() {
        int brightness = (int) this.brightness + 1;
        this.brightness = Math.clamp(0,PRESETS.length-1,brightness);
    }
    
    public float brightness() {
        return brightness / PRESETS.length;
    }
    
    public void togglePointLight() {
        pointLight = pointLight == 0.0f ? 1.0f : 0.0f;
    }
    
    public boolean usingPointLight() {
        return pointLight != 0.0f;
    }
    
    public void dispose() {
        Disposable.dispose(uniformBuffer);
    }
    
}
