package io.github.heathensoft.canvas.brush;

import io.github.heathensoft.canvas.Channel;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.graphics.TextureFormat;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_NEAREST;


/**
 * @author Frederik Dahl
 * 28/01/2023
 */


public class Brush implements Disposable {
    
    public static final int TEXTURE_SIZE = 64; // must be pow2
    
    public enum Tool {
        SAMPLER(0,"Sampler"),
        FREE_HAND(1,"Free Hand"),
        LINE_DRAW(2,"Line Draw"),
        DRAG_AREA(3,"Drag Area");
        public static final String DESCRIPTOR = "Tool";
        public static final Tool DEFAULT = FREE_HAND;
        public static final Tool[] ALL = values();
        public static final int SIZE = ALL.length;
        public final String descriptor;
        public final int id;
        public Tool next() {
            return ALL[(id + 1) % SIZE];
        }
        public Tool prev() {
            return id == 0 ? ALL[(SIZE - 1)] : ALL[id - 1];
        }
        Tool(int id, String descriptor) {
            this.descriptor = descriptor;
            this.id = id;
        }
    }
    
    public enum Shape {
        ROUND(0,"Round"),
        SQUARE(1,"Square");
        public static final String DESCRIPTOR = "Shape";
        public static final Shape DEFAULT = ROUND;
        public static final Shape[] ALL = values();
        public static final int SIZE = ALL.length;
        public final String descriptor;
        public final int id;
        public Shape next() {
            return ALL[(id + 1) % SIZE];
        }
        public Shape prev() {
            return id == 0 ? ALL[(SIZE - 1)] : ALL[id - 1];
        }
        Shape(int id, String descriptor) {
            this.descriptor = descriptor;
            this.id = id;
        }
    }
    
    public enum Function {
        NON(0,"None"),
        SET(1,"Set"),
        ADD(2,"Add"),
        SUB(3,"Subtract"),
        MIX(4,"Mix"),
        SMOOTHEN(5,"Smoothen"),
        SHARPEN(6,"Sharpen");
        public static final String DESCRIPTOR = "Function";
        public static final Function DEFAULT = SET;
        public static final Function[] ALL = values();
        public static final int SIZE = ALL.length;
        public final String descriptor;
        public final int id;
        public Function next() {
            return ALL[(id + 1) % SIZE];
        }
        public Function prev() {
            return id == 0 ? ALL[(SIZE - 1)] : ALL[id - 1];
        }
        Function(int id, String descriptor) {
            this.descriptor = descriptor;
            this.id = id;
        }
    }
    
    private int color;
    private int brush_size;
    private final int texture_size;
    
    private Tool tool;
    private Shape shape;
    private Function function;
    private final Texture texture;
    private final Color contourColor;
    
    public Brush() {
        this(Shape.ROUND,Tool.FREE_HAND,Function.SET,1);
    }
    
    
    public Brush(Shape shape, Tool tool, Function function, int size) {
        this.contourColor = Color.GREEN.cpy();
        this.texture_size = TEXTURE_SIZE;
        this.brush_size = clampBrushSize(size);
        this.function = function;
        this.shape = shape;
        this.tool = tool;
        this.color = 255;
        
        this.texture = Texture.generate2D(texture_size,texture_size);
        this.texture.bindToActiveSlot();
        this.texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
        this.texture.filter(GL_NEAREST,GL_NEAREST);
        this.texture.clampToEdge();
        
        try (MemoryStack stack = MemoryStack.stackPush()){
            ByteBuffer pixels = stack.malloc(texture_size * texture_size);
            if (shape == Shape.ROUND) {
                boolean oddSize = (brush_size & 1) == 1;
                float offset = oddSize ? 0.5f : 0.0f;
                float radius2 = (brush_size / 2.f) * (brush_size / 2.f);
                float center_x = (texture_size / 2f) - offset;
                float center_y = (texture_size / 2f) - offset;
                for (int r = 0; r < texture_size; r++) {
                    float y = r + 0.5f;
                    float dy = center_y - y;
                    for (int c = 0; c < texture_size; c++) {
                        float x = c + 0.5f;
                        float dx = center_x - x;
                        float d2 = dx * dx + dy * dy;
                        if (radius2 >= d2) pixels.put((byte) 255);
                        else pixels.put((byte)0);
                    }
                }
            } else {
                Area area = new Area(0,0,brush_size-1,brush_size-1);
                int translationX = ((texture_size - brush_size) / 2);
                int translationY = ((texture_size - brush_size) / 2);
                area.translate(translationX,translationY);
                for (int r = 0; r < texture_size; r++) {
                    for (int c = 0; c < texture_size; c++) {
                        if (area.contains(c,r)) pixels.put((byte) 255);
                        else pixels.put((byte)0);
                    }
                }
            }
            texture.uploadData(pixels.flip());
            //stbi_write_png("brush.png",texture_size,texture_size,1,pixels,texture_size);
        }
    }
    
    
    
    public int brushSize() {
        return brush_size;
    }
    
    public void decrementBrushSize(int amount) {
        setBrushSize(brush_size - amount);
    }
    
    public void incrementBrushSize(int amount) {
        setBrushSize(brush_size + amount);
    }
    
    public void setBrushSize(int size) {
        size = clampBrushSize(size);
        if (brush_size != size) {
            brush_size = size;
            refreshTexture();
        }
    }
    
    private int clampBrushSize(int size) {
        size = Math.min(size,texture_size);
        size = Math.max(size,1);
        return size;
    }
    
    
    
    public int colorValue() {
        return color;
    }
    
    public void decrementColor(int amount) {
        setColor(color - amount);
    }
    
    public void incrementColor(int amount) {
        setColor(color + amount);
    }
    
    public void setColor(int value) {
        color = clampColor(value);
    }
    
    private int clampColor(int value) {
        value = Math.min(value,255);
        value = Math.max(value,0);
        return value;
    }
    
    
    
    public int textureSize() {
        return texture_size;
    }
    
    public Texture texture() {
        return texture;
    }
    
    
    
    public Tool tool() {
        return tool;
    }
    
    public void setTool(Tool tool) {
        this.tool = tool;
    }
    
    
    public Shape shape() {
        return shape;
    }
    
    public void setShape(Shape shape) {
        if (this.shape != shape) {
            this.shape = shape;
            refreshTexture();
        }
    }
    
    public Function function() {
        return function;
    }
    
    public void setFunction(Function function) {
        this.function = function;
    }
    
    public Color contourColor() {
        return contourColor;
    }
    
    public void setContourColor(Color color) {
        this.contourColor.set(color);
    }
    
    
    private void refreshTexture() {
        try (MemoryStack stack = MemoryStack.stackPush()){
            ByteBuffer pixels = stack.malloc(texture_size * texture_size);
            if (shape == Shape.ROUND) {
                boolean oddSize = (brush_size & 1) == 1;
                float offset = oddSize ? 0.5f : 0.0f;
                float radius2 = (brush_size / 2.f) * (brush_size / 2.f);
                float center_x = (texture_size / 2f) - offset;
                float center_y = (texture_size / 2f) - offset;
                for (int r = 0; r < texture_size; r++) {
                    float y = r + 0.5f;
                    float dy = center_y - y;
                    for (int c = 0; c < texture_size; c++) {
                        float x = c + 0.5f;
                        float dx = center_x - x;
                        float d2 = dx * dx + dy * dy;
                        if (radius2 >= d2) pixels.put((byte) 255);
                        else pixels.put((byte)0);
                    }
                }
            } else {
                Area area = new Area(0,0,brush_size-1,brush_size-1);
                int translationX = ((texture_size - brush_size) / 2);
                int translationY = ((texture_size - brush_size) / 2);
                area.translate(translationX,translationY);
                for (int r = 0; r < texture_size; r++) {
                    for (int c = 0; c < texture_size; c++) {
                        if (area.contains(c,r)) pixels.put((byte) 255);
                        else pixels.put((byte)0);
                    }
                }
            }
            texture.bindToActiveSlot();
            texture.uploadSubData(pixels.flip(),0);
        }
    }
    
    public void dispose() {
        Disposable.dispose(texture);
    }
    
}
