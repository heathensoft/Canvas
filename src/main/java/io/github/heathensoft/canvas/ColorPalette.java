package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frederik Dahl
 * 27/01/2023
 */


public class ColorPalette implements Disposable {
    
    public static final String DEFAULT = "bright_future";
    
    private static final int TEXTURE_SIZE = 64;
    private static final String UNTITLED = "untitled";
    private static final String PALETTE_RESOURCE_DIRECTORY = "res/palettes";
    private static final Map<String,ColorPalette> palettes = new HashMap<>(31);
    
    private final String name;
    private final Texture texture;
    
    
    public ColorPalette(List<Color> colors) {
        this(null,colors);
    }
    
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
    
    public static void loadResources() throws IOException {
        Resources io = new Resources(ColorPalette.class);
        List<String> resourceFiles = io.getResourceFiles(PALETTE_RESOURCE_DIRECTORY);
        for (String fileName : resourceFiles) {
            Path path = Path.of(PALETTE_RESOURCE_DIRECTORY).resolve(fileName);
            String[] split = path.getFileName().toString().split("\\.");
            String name = split.length != 2 ? null : split[0];
            List<String> lines = io.asLines(path.toString());
            if (!lines.isEmpty()) {
                List<Color> colors = new ArrayList<>(lines.size());
                for (String line : lines) {
                    Color color = Color.valueOf(line);
                    colors.add(color);
                } new ColorPalette(name, colors);
            }
        }
    }
}
