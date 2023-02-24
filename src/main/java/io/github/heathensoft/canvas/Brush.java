package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 28/01/2023
 */


public class Brush implements Disposable {
    
    public static final int BINDING_POINT = 3;
    public static final int UNIFORM_BLOCK_SIZE = 8;
    public static final int TEXTURE_SIZE = 64; // must be pow2
    public static final int DEFAULT_SIZE = 5;
    public static final int DEFAULT_COLOR = 0xFF;
    
    private static Brush instance;
    
    public static Brush get() {
        if (instance == null) {
            instance = new Brush();
        } return instance;
    }
    
    private int color;
    private int brush_size;
    private final int texture_size;
    
    private ENUM.BrushTool tool;
    private ENUM.BrushShape shape;
    private ENUM.BrushFunction function;
    private final StrokeBuffer strokeBuffer;
    private final Texture texture;
    private final Color contourColor;
    private final BufferObject uniformBuffer;
    
    private Brush() {
        this(ENUM.BrushShape.DEFAULT, ENUM.BrushTool.DEFAULT, ENUM.BrushFunction.DEFAULT,DEFAULT_SIZE);
    }
    
    private Brush(ENUM.BrushShape shape, ENUM.BrushTool tool, ENUM.BrushFunction function, int size) {
        this.contourColor = Color.GREEN.cpy();
        this.texture_size = TEXTURE_SIZE;
        this.color = DEFAULT_COLOR;
        this.brush_size = clampBrushSize(size);
        this.function = function;
        this.shape = shape;
        this.tool = tool;
    
        this.strokeBuffer = new StrokeBuffer(256);
        this.uniformBuffer = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW);
        this.uniformBuffer.bind();
        this.uniformBuffer.bufferData((long) UNIFORM_BLOCK_SIZE * Integer.BYTES);
        this.uniformBuffer.bindBufferBase(BINDING_POINT);
        
        this.texture = Texture.generate2D(texture_size,texture_size);
        this.texture.bindToActiveSlot();
        this.texture.allocate(TextureFormat.R8_UNSIGNED_NORMALIZED,false);
        this.texture.filter(GL_NEAREST,GL_NEAREST);
        this.texture.clampToEdge();
        
        try (MemoryStack stack = MemoryStack.stackPush()){
            ByteBuffer pixels = stack.malloc(texture_size * texture_size);
            if (shape == ENUM.BrushShape.ROUND) {
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
        }
    }
    
    public void uploadUniformBlock() {
        uniformBuffer.bind();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(UNIFORM_BLOCK_SIZE);
            buffer.put(texture_size).put(texture_size).put(function.id);
            buffer.put(color).put(tool.id).put(shape.id).put(brush_size).put(0);
            uniformBuffer.bufferSubData(buffer.flip(),0);
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
    
    public ENUM.BrushTool tool() {
        return tool;
    }
    
    public void setTool(ENUM.BrushTool tool) {
        this.tool = tool;
    }
    
    public ENUM.BrushShape shape() {
        return shape;
    }
    
    public void toggleShape() {
        setShape(shape.next());
    }
    
    public void setShape(ENUM.BrushShape shape) {
        if (this.shape != shape) {
            this.shape = shape;
            refreshTexture();
        }
    }
    
    public StrokeBuffer strokeBuffer() {
        return strokeBuffer;
    }
    
    public ENUM.BrushFunction function() {
        return function;
    }
    
    public void setFunction(ENUM.BrushFunction function) {
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
            if (shape == ENUM.BrushShape.ROUND) {
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
        Disposable.dispose(
                texture,
                uniformBuffer,
                strokeBuffer);
    }
    
    public static final class StrokeBuffer implements Disposable {
        
        private final Vao vertexArrayObject;
        private final BufferObject vertexBuffer;
        private final FloatBuffer vertices;
        private final int vertex_capacity;
        private int count;
        
        
        public StrokeBuffer(int vertex_capacity) {
            this.vertex_capacity = vertex_capacity;
            this.vertices = MemoryUtil.memAllocFloat(vertex_capacity * 2);
            this.vertexBuffer = new BufferObject(GL_ARRAY_BUFFER,GL_DYNAMIC_DRAW);
            this.vertexArrayObject = new Vao().bind();
            this.vertexBuffer.bind().bufferData((long) 2 * vertex_capacity * Float.BYTES);
            glVertexAttribPointer(0,2,GL_FLOAT,false,2 * Float.BYTES,0);
            glEnableVertexAttribArray(0);
        }
        
        
        public void put(float x, float y) {
            if (count < vertex_capacity) {
                vertices.put(x).put(y);
                count++;
            }
        }
        
        
        public void upload() {
            if (count > 0) {
                vertices.flip();
                vertexArrayObject.bind();
                vertexBuffer.bind();
                vertexBuffer.bufferSubData(vertices,0);
                glDrawArrays(GL_POINTS,0,count);
                vertices.clear();
                count = 0;
            }
        }
        
        
        @Override
        public void dispose() {
            if (vertices != null) MemoryUtil.memFree(vertices);
            Disposable.dispose(vertexArrayObject,vertexBuffer);
        }
    }
}
