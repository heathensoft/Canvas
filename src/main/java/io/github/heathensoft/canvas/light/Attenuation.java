package io.github.heathensoft.canvas.light;

import java.nio.FloatBuffer;

/**
 * @author Frederik Dahl
 * 02/12/2021
 */


public class Attenuation {
    
    // https://wiki.ogre3d.org/tiki-index.php?page=-Point+Light+Attenuation
    
    public static final Attenuation DEFAULT;
    public static final Attenuation[] PRESETS;
    public static final Attenuation ATT_100 = new Attenuation(1.0f,0.045f,0.0075f);
    public static final Attenuation ATT_160 = new Attenuation(1.0f,0.027f,0.0028f);
    public static final Attenuation ATT_200 = new Attenuation(1.0f,0.022f,0.0019f);
    public static final Attenuation ATT_325 = new Attenuation(1.0f,0.014f,0.0007f);
    public static final Attenuation ATT_600 = new Attenuation(1.0f,0.007f,0.0002f);
    public static final Attenuation ATT_3250 = new Attenuation(1.0f,0.0014f,0.000007f);
    
    static {
        DEFAULT = ATT_600;
        PRESETS = new Attenuation[] {ATT_100,ATT_160,ATT_200,ATT_325,ATT_600,ATT_3250};
    }
    
    public static void getPresets(FloatBuffer buffer) {
        for (Attenuation preset : PRESETS) {
            preset.get(buffer);
        }
    }
    
    private float constant;
    private float linear;
    private float quadratic;
    
    public Attenuation() {
        this(DEFAULT);
    }
    
    public Attenuation(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }
    
    public Attenuation(Attenuation attenuation) {
        this.constant = attenuation.constant;
        this.linear = attenuation.linear;
        this.quadratic = attenuation.quadratic;
    }
    
    public void set(Attenuation attenuation) {
        if (attenuation != null) {
            this.constant = attenuation.constant;
            this.linear = attenuation.linear;
            this.quadratic = attenuation.quadratic;
        }
    }
    
    public void set(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }
    
    public void get(FloatBuffer buffer) {
        buffer.put(constant).put(linear).put(quadratic).put(0);
    }
    
    
    
    public float constant() {
        return constant;
    }
    
    public float linear() {
        return linear;
    }
    
    public float quadratic() {
        return quadratic;
    }
    
    public void setConstant(float constant) {
        this.constant = constant;
    }
    
    public void setLinear(float linear) {
        this.linear = linear;
    }
    
    public void setQuadratic(float quadratic) {
        this.quadratic = quadratic;
    }
    
}
