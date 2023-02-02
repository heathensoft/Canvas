package io.github.heathensoft.canvas;

/**
 * @author Frederik Dahl
 * 24/01/2023
 */


public enum Channel {
    DETAILS(0,"Details"),
    VOLUME(1,"Volume"),
    SPECULAR(2,"Specular"),
    EMISSIVE(3,"Emissive");
    public static final String DESCRIPTOR = "Channel";
    public static final Channel DEFAULT = DETAILS;
    public static final Channel[] ALL = values();
    public static final int SIZE = ALL.length;
    public final String descriptor;
    public final int id;
    public Channel next() {
        return ALL[(id + 1) % SIZE];
    }
    public Channel prev() {
        return id == 0 ? ALL[(SIZE - 1)] : ALL[id - 1];
    }
    Channel(int id, String descriptor) {
        this.descriptor = descriptor;
        this.id = id;
    }
}
