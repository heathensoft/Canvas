package io.github.heathensoft.canvas.neo;

/**
 * @author Frederik Dahl
 * 16/02/2023
 */


public class ENUM {
    
    public enum Channel {
        DEPTH_1(0,"Depth 1"),
        DEPTH_2(1,"Depth 2"),
        SPECULAR(2,"Specular"),
        EMISSIVE(3,"Emissive");
        public static final String DESCRIPTOR = "Channel";
        public static final Channel DEFAULT = DEPTH_1;
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
}
