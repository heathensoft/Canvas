package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.debug.DebugLines2D;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import org.joml.primitives.Rectanglef;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 09/01/2023
 */


public class CanvasGrid implements Disposable {
    
    private final Color gridColor;
    private final Color axisColor;
    private boolean show;
    private int grid_size;
    
    
    public CanvasGrid() {
        DebugLines2D.initialize();
        gridColor = Color.WHITE.cpy();
        axisColor = Color.WHITE.cpy();
        gridColor.a = 0.5f;
        grid_size = 16;
        show = true;
    }
    
    public void draw(OrthographicCamera camera) {
        // use: split screen framebuffer. draw to left
        if (show) {
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
            DebugLines2D.begin(camera.combined());
            float grid_dt = grid_size / camera.zoom;
            // distance between lines >= 8 pixels on screen
            if (grid_dt >= 8.0) {
                DebugLines2D.setColor(gridColor);
                DebugLines2D.drawGrid(camera,grid_size,false,false);
            } DebugLines2D.setColor(axisColor);
            Rectanglef bounds = camera.bounds;
            if (bounds.minX < 0 && bounds.maxX > 0) {
                DebugLines2D.drawVertical(bounds.minY,bounds.maxY,0);
            } if (bounds.minY < 0 && bounds.maxY > 0) {
                DebugLines2D.drawHorizontal(bounds.minX,bounds.maxX,0);
            } DebugLines2D.end();
        }
    }
    
    public int gridSize() {
        return grid_size;
    }
    
    public void incrementSize() {
        int size = grid_size + 1;
        setSize(size);
    }
    
    public void decrementSize() {
        int size = grid_size - 1;
        setSize(size);
    }
    
    public void setSize(int size) {
        this.grid_size = Math.max(1,size);
    }
    
    public void setGridColor(Color color) {
        this.gridColor.set(color);
    }
    
    public void setAxisColor(Color color) {
        this.axisColor.set(color);
    }
    
    public void show() {
        show = true;
    }
    
    public void hide() {
        show = false;
    }
    
    public void dispose() {
        DebugLines2D.dispose();
    }
}
