package io.github.heathensoft.canvas.f;

import io.github.heathensoft.canvas.CanvasBackground;
import io.github.heathensoft.canvas.CanvasGrid;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Coordinate;

/**
 * @author Frederik Dahl
 * 05/02/2023
 */


public class EditorGraphics implements Disposable {
    
    
    private Editor editor;
    private CanvasGrid grid;
    private CanvasBackground background;
    
    
    public void projectPipeline() {
    
    }
    
    public void renderSplitScreen() {
    
    }
    
    public void drawToBackbuffer() {
    
    }
    
    public CanvasBackground background() {
        return background;
    }
    
    public CanvasGrid grid() {
        return grid;
    }
    
    
    public void readPixel(Coordinate coordinate) {
    
    }
    
    public int pixelValue() {
        return 255;
    }
    
    public void dispose() {
    
    }
}
