package io.github.heathensoft.canvas.brush;

/**
 * @author Frederik Dahl
 * 24/01/2023
 */


public enum Channel {
    
    DETAILS(0,"Details"),
    VOLUME(0,"Volume"),
    SPECULAR(2,"Specular"),
    EMISSIVE(3,"Emissive");
    
    public final int idx;
    public final String descriptor;
    
    Channel(int idx, String descriptor) {
        this.descriptor = descriptor;
        this.idx = idx;
    }
}
