package io.github.heathensoft.canvas.io;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Can also be loaded from: .hex, .txt, .palette
 *
 * @author Frederik Dahl
 * 25/01/2023
 */


public class Palette implements Disposable {
    
    
    private static final List<String> validExtensions;
    
    static {
        validExtensions = new ArrayList<>(3);
        validExtensions.add("hex");
        validExtensions.add("txt");
        validExtensions.add("palette");
    }
    
    private final List<Color> colors;
    private final Texture palette;
    private final String name;
    
    public Palette(List<Color> colors) {
        this("untitled",colors);
    }
    
    public Palette(String name, List<Color> colors) {
        if (colors.isEmpty()) colors.add(Color.WHITE.cpy());
        this.name = name;
        this.colors = colors;
        int texture_size = colors.size() > 256 ? 128 : 64;
        this.palette = Color.paletteSampler3D(colors,texture_size);
    }
    
    public List<Color> colors() {
        return colors;
    }
    
    public Texture texture() {
        return palette;
    }
    
    public String name() {
        return name;
    }
    
    public void dispose() {
        Disposable.dispose(palette);
    }
    
    public static void toFile(Palette palette, Path directory) throws Exception {
        External folder = new External(directory);
        if (folder.isFolder()) {
            List<String> lines = new ArrayList<>(palette.colors().size());
            for (Color color : palette.colors()) {
                lines.add(color.toString());
            } External file = new External(folder.path().resolve(palette.name + ".hex"));
            if (!file.exist()) {
                file.createFile(true);
            } file.write(lines);
        } throw new Exception("failed to save palette to folder: " + folder.path().toString());
    }
    
    public static Palette fromFile(String path) throws Exception {
        return fromFile(Path.of(path));
    }
    
    public static Palette fromFile(Path path) throws Exception {
        External file = new External(path);
        failedToReadPalette:
        if(file.isFile()) {
            String[] split = file.name().split("\\.");
            if (split.length != 2) break failedToReadPalette;
            String extension = split[1].toLowerCase();
            if (validExtensions.contains(extension)) {
                String paletteName = split[0];
                List<String> lines = file.readLinesToList();
                List<Color> colors = new ArrayList<>(lines.size());
                try { for (String line : lines) {
                    if (!line.isBlank()) {
                        Color color = Color.valueOf(line);
                        colors.add(color);
                    }
                }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    break failedToReadPalette;
                }
                if (colors.isEmpty())
                    break failedToReadPalette;
                return new Palette(paletteName,colors);
            }
        } throw new Exception("failed to read palette");
    }
}
