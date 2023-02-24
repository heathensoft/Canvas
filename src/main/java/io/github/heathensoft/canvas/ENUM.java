package io.github.heathensoft.canvas;


/**
 * @author Frederik Dahl
 * 03/02/2023
 */


public class ENUM {
    
    
    public enum ImportStatus {
        INCOMPLETE("Color image missing",false),
        COMPLETE_READY("All images loaded",true),
        INCOMPLETE_READY("Incomplete, ready",true),
        SIZES_NOT_MATCHING("Image sizes not matching",false);
        public final String description;
        public final boolean ready;
        ImportStatus(String description, boolean ready) {
            this.description = description;
            this.ready = ready;
        }
    }
    
    public enum PreviewDisplay {
        SOURCE(0,0,"Color Source"),
        DEPTH_MAP(1,1,"Depth Map"),
        NORMAL_MAP(2,2,"Normal Map"),
        SHADOW_MAP(3,4,"Shadow Map");
        public static final String DESCRIPTOR = "Preview Display";
        public static final PreviewDisplay DEFAULT = SOURCE;
        public static final PreviewDisplay[] ALL = values();
        public static final int SIZE = ALL.length;
        public final String descriptor;
        public final int value;
        public final int id;
        public PreviewDisplay next() {
            return ALL[(id + 1) % SIZE];
        } public PreviewDisplay prev() {
            return id == 0 ? ALL[(SIZE - 1)] : ALL[id - 1];
        } PreviewDisplay(int id, int value, String descriptor) {
            this.descriptor = descriptor;
            this.value = value;
            this.id = id;
        }
    }
    
    public enum Channel {
        DETAILS(0,"Details"),
        VOLUME(1,"Volume"),
        SPECULAR(2,"Specular"),
        EMISSIVE(3,"Emissive");
        public static final String DESCRIPTOR = "Channel";
        public static final Channel DEFAULT = VOLUME;
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
    
    public enum BrushTool {
        SAMPLER(0,"Sampler"),
        FREE_HAND(1,"Free Hand"),
        LINE_DRAW(2,"Line Draw"),
        DRAG_AREA(3,"Drag Area");
        public static final String DESCRIPTOR = "Brush Tool";
        public static final BrushTool DEFAULT = FREE_HAND;
        public static final BrushTool[] ALL = values();
        public static final int SIZE = ALL.length;
        public final String descriptor;
        public final int id;
        public BrushTool next() {
            return ALL[(id + 1) % SIZE];
        }
        public BrushTool prev() {
            return id == 0 ? ALL[(SIZE - 1)] : ALL[id - 1];
        }
        BrushTool(int id, String descriptor) {
            this.descriptor = descriptor;
            this.id = id;
        }
    }
    
    public enum BrushShape {
        ROUND(0,"Round"),
        SQUARE(1,"Square");
        public static final String DESCRIPTOR = "Brush Shape";
        public static final BrushShape DEFAULT = ROUND;
        public static final BrushShape[] ALL = values();
        public static final int SIZE = ALL.length;
        public final String descriptor;
        public final int id;
        public BrushShape next() {
            return ALL[(id + 1) % SIZE];
        }
        public BrushShape prev() {
            return id == 0 ? ALL[(SIZE - 1)] : ALL[id - 1];
        }
        BrushShape(int id, String descriptor) {
            this.descriptor = descriptor;
            this.id = id;
        }
    }
    
    public enum BrushFunction {
        NON(0,"None"),
        SET(1,"Set"),
        ADD(2,"Add"),
        SUB(3,"Subtract"),
        MIX(4,"Mix"),
        SMOOTHEN(5,"Smoothen"),
        SHARPEN(6,"Sharpen"),
        RAISE(7,"Raise"),
        LOWER(8,"Lower");
        public static final String DESCRIPTOR = "Brush Function";
        public static final BrushFunction DEFAULT = SET;
        public static final BrushFunction[] ALL = values();
        public static final int SIZE = ALL.length;
        public final String descriptor;
        public final int id;
        public BrushFunction next() {
            return ALL[(id + 1) % SIZE];
        } public BrushFunction prev() {
            return id == 0 ? ALL[(SIZE - 1)] : ALL[id - 1];
        } BrushFunction(int id, String descriptor) {
            this.descriptor = descriptor;
            this.id = id;
        }
    }
}
