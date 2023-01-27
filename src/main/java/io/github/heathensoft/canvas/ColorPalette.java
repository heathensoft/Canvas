package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frederik Dahl
 * 27/01/2023
 */


public class ColorPalette implements Disposable {
    
    private static final int TEXTURE_SIZE = 64;
    private static final String UNTITLED = "untitled";
    private static final Map<String,ColorPalette> palettes = new HashMap<>(31);
    
    private final String name;
    private final Texture texture;
    
    
    public ColorPalette(String name, List<Color> colors) {
        if (colors.isEmpty()) colors.add(Color.WHITE.cpy());
        this.texture = Color.paletteSampler3D(colors,TEXTURE_SIZE);
        this.name = validateName(name);
        System.out.println(name);
        palettes.put(name,this);
    }
    
    public String name() {
        return name;
    }
    
    public Texture texture() {
        return texture;
    }
    
    public void dispose() {
        palettes.remove(name);
        Disposable.dispose(texture);
    }
    
    private String validateName(String name) {
        name = name == null || name.isBlank() ? UNTITLED : name;
        String rootName = name;
        int numerator = 1;
        while (palettes.containsKey(name)) {
            name = rootName + "_" + numerator;
            numerator++;
        } return name;
    }
    
    public static ColorPalette get(String name) {
        return palettes.get(name);
    }
    
    public static Map<String,ColorPalette> map() {
        return palettes;
    }
    
    public static void disposeAll() {
        if (!palettes.isEmpty()) {
            List<ColorPalette> list = new ArrayList<>(palettes.values());
            for (ColorPalette palette : list) palette.dispose();
        }
        
    }
}
