package io.github.heathensoft.canvas.brush;

import io.github.heathensoft.jlib.lwjgl.graphics.Color;


/**
 * @author Frederik Dahl
 * 28/01/2023
 */


public class Brush {
    
    private int color_value;
    private int texture_size;
    private Tool tool;
    private Shape shape;
    private Function function;
    private Color contourColor;
    
    
    public int color_value() {
        return color_value;
    }
    
    public int texture_size() {
        return texture_size;
    }
    
    public Tool tool() {
        return tool;
    }
    
    public Shape shape() {
        return shape;
    }
    
    public Function function() {
        return function;
    }
    
    public Color contourColor() {
        return contourColor;
    }
    
    
    public enum Tool {
        SAMPLER("Sampler"),
        FREE_HAND("Free Hand"),
        LINE_DRAW("Line Draw"),
        RECTANGLE("Rectangle");
        public final String description;
        Tool(String description) {
            this.description = description;
        }
    }
    
    public enum Shape {
        ROUND("Round"),
        SQUARE("Square");
        public final String description;
        Shape(String description) {
            this.description = description;
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
        public final String description;
        public final int id;
        Function(int id, String description) {
            this.description = description;
            this.id = id;
        }
    }
    
    
}
