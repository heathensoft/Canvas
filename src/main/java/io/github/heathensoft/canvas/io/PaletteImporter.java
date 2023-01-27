package io.github.heathensoft.canvas.io;

import io.github.heathensoft.canvas.ColorPalette;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 27/01/2023
 */


public class PaletteImporter {
 
    public static final String PALETTE_RESOURCE_DIRECTORY = "res/palettes";
    
    
    public static void loadResources() throws IOException {
        Resources io = new Resources(PaletteImporter.class);
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
