package io.github.heathensoft.canvas.old.brushold;

/**
 * @author Frederik Dahl
 * 22/01/2023
 */


public enum Channel {
    
    DEPTH(0,"Depth"),
    SPECULAR(1,"Specular"),
    EMISSIVE(2,"Emissive");
    
    public final int idx;
    public final String descriptor;
    
    Channel(int idx, String descriptor) {
        this.descriptor = descriptor;
        this.idx = idx;
    }
}
