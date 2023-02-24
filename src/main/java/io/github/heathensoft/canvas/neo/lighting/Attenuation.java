package io.github.heathensoft.canvas.neo.lighting;

import java.nio.FloatBuffer;
import java.util.Objects;

/**
 * @author Frederik Dahl
 * 22/02/2023
 */

// https://wiki.ogre3d.org/tiki-index.php?page=-Point+Light+Attenuation

public class Attenuation {
    
    public static final Attenuation ATT_100 = new Attenuation(1.0f, 0.045f, 0.0075f);
    public static final Attenuation ATT_160 = new Attenuation(1.0f, 0.027f, 0.0028f);
    public static final Attenuation ATT_200 = new Attenuation(1.0f, 0.022f, 0.0019f);
    public static final Attenuation ATT_325 = new Attenuation(1.0f, 0.014f, 0.0007f);
    public static final Attenuation ATT_600 = new Attenuation(1.0f, 0.007f, 0.0002f);
    public static final Attenuation ATT_3250 = new Attenuation(1.0f, 0.0014f, 0.000007f);
    
    public float constant;
    public float linear;
    public float quadratic;
    
    public Attenuation(Attenuation attenuation) {
        set(attenuation);
    }
    
    public Attenuation(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }
    
    public void get(FloatBuffer buffer) {
        buffer.put(constant).put(linear).put(quadratic).put(0);
    }
    
    public void set(Attenuation attenuation) {
        this.constant = attenuation.constant;
        this.linear = attenuation.linear;
        this.quadratic = attenuation.quadratic;
    }
    
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Attenuation) obj;
        return Float.floatToIntBits(this.constant) == Float.floatToIntBits(that.constant) &&
                       Float.floatToIntBits(this.linear) == Float.floatToIntBits(that.linear) &&
                       Float.floatToIntBits(this.quadratic) == Float.floatToIntBits(that.quadratic);
    }
    
    
    public int hashCode() {
        return Objects.hash(constant, linear, quadratic);
    }
    
}
