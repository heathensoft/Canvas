package io.github.heathensoft.canvas;

import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.UndoRedo;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * @author Frederik Dahl
 * 02/02/2023
 */


public class UndoRedoManager implements Disposable {
 
    public static final int CAPACITY = 8;
    
    private final UndoRedo<UndoRedoObject> stack;
    private final Project project;
    
    
    public UndoRedoManager(Project project) {
        this.stack = new UndoRedo<>(CAPACITY);
        this.project = project;
    }
    
    public void newEdit(Area editArea, ENUM.Channel channel, Brush brush) {
        UndoRedoObject object = createObject(editArea,channel,brush.tool(),brush.function());
        stack.onEdit(object);
    }
    
    public void undo() {
        UndoRedoObject undoObject = stack.peakUndo();
        if (undoObject != null) {
            Area editArea = new Area(undoObject.editArea());
            ENUM.BrushFunction function = undoObject.function();
            ENUM.Channel channel = undoObject.channel();
            ENUM.BrushTool tool = undoObject.tool();
            UndoRedoObject redoObject = createObject(editArea,channel,tool,function);
            undoObject = stack.undo(redoObject);
            undoObject.upload();
            undoObject.dispose();
        }
    }
    
    public void redo() {
        UndoRedoObject redoObject = stack.peakRedo();
        if (redoObject != null) {
            Area editArea = new Area(redoObject.editArea());
            ENUM.BrushFunction function = redoObject.function();
            ENUM.Channel channel = redoObject.channel();
            ENUM.BrushTool tool = redoObject.tool();
            UndoRedoObject undoObject = createObject(editArea,channel,tool,function);
            redoObject = stack.redo(undoObject);
            redoObject.upload();
            redoObject.dispose();
        }
    }
    
    public UndoRedoObject peakUndo() {
        return stack.peakUndo();
    }
    
    public UndoRedoObject peakRedo() {
        return stack.peakRedo();
    }
    
    public boolean canUndo() {
        return stack.canUndo();
    }
    
    public boolean canRedo() {
        return stack.canRedo();
    }
    
    public int undoCount() {
        return stack.undoCount();
    }
    
    public int redoCount() {
        return stack.redoCount();
    }
    
    public void dispose() {
        Disposable.dispose(stack);
    }
    
    private UndoRedoObject createObject(Area editArea, ENUM.Channel channel,
                                        ENUM.BrushTool tool, ENUM.BrushFunction function) {
        
        int tex_w = project.texturesWidth();
        int tex_h = project.texturesHeight();
        int x_offset = -(int)project.bounds().x;
        int y_offset = -(int)project.bounds().y;
        Area textureArea = new Area(0,0,tex_w,tex_h); // wrong
        editArea.translate(x_offset,y_offset);
        if (!textureArea.intersection(editArea)) {
            Logger.warn("should not happen");
            editArea.set(textureArea);
        } return new UndoRedoObject(
                project.backBuffer(), editArea, channel, tool, function);
        
    }
    
    public static final class UndoRedoObject implements Disposable {
        
        private final Area editArea;
        private final ENUM.Channel channel;
        private final ENUM.BrushTool tool;
        private final ByteBuffer buffer;
        private final Framebuffer backBuffer;
        private final ENUM.BrushFunction function;
        
        UndoRedoObject(Framebuffer backBuffer, Area editArea, ENUM.Channel channel,
                       ENUM.BrushTool tool, ENUM.BrushFunction function) {
            
            this.buffer = MemoryUtil.memAlloc(editArea.size() + 16);
            this.editArea = editArea;
            this.channel = channel;
            this.tool = tool;
            this.function = function;
            this.backBuffer = backBuffer;
            
            int x0 = editArea.minX();
            int y0 = editArea.minY();
            int width = editArea.cols();
            int height = editArea.rows();
            Framebuffer.bindRead(backBuffer);
            Framebuffer.readBuffer(channel.id);
            glPixelStorei(GL_PACK_ALIGNMENT,1);
            glReadPixels(x0,y0,width,height,GL_RED,GL_UNSIGNED_BYTE, buffer);
        }
        
        public void upload() {
            int x0 = editArea.minX();
            int y0 = editArea.minY();
            int width = editArea.cols();
            int height = editArea.rows();
            Texture texture = backBuffer.texture(channel.id);
            texture.bindToActiveSlot();
            texture.uploadSubData(buffer,0,width,height,x0,y0);
        }
        
        public Area editArea() {
            return editArea;
        }
        
        public ENUM.Channel channel() {
            return channel;
        }
        
        public ENUM.BrushTool tool() {
            return tool;
        }
        
        public ENUM.BrushFunction function() {
            return function;
        }
        
        public int sizeOf() {
            return buffer.capacity();
        }
        
        public void dispose() {
            if (buffer != null) MemoryUtil.memFree(buffer);
        }
    }
    
}
