package io.github.heathensoft.canvas.neo;

import io.github.heathensoft.canvas.Brush;
import io.github.heathensoft.canvas.ENUM;
import io.github.heathensoft.canvas.Project;
import io.github.heathensoft.jlib.common.storage.primitive.ByteArray2D;
import io.github.heathensoft.jlib.common.storage.primitive.IntQueue;
import io.github.heathensoft.jlib.common.storage.primitive.iterators.ByteReader;
import io.github.heathensoft.jlib.common.thread.Task;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.BooleanGrid;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 17/02/2023
 */


public class FloodFillTask implements Task {
    
    private byte fillValue;
    private Project project;
    private ENUM.Channel channel;
    private Coordinate origin; // grid x0,y0 has to be 0,0
    private BooleanGrid visited;
    private ByteArray2D pixels;
    private Area editArea;
    
    
    public FloodFillTask(Project project, ENUM.Channel channel, Coordinate cursor, byte fillValue) {
        this.origin = new Coordinate(cursor);
        this.origin.sub(project.offsetFromOrigin());
        this.project = project;
        this.fillValue = fillValue;
        this.editArea = new Area(origin);
        this.channel = channel;
        Area textureArea = new Area(project.area());
        textureArea.translate(project.offsetFromOrigin());
        this.visited = new BooleanGrid(textureArea);
        int w = textureArea.cols();
        int h = textureArea.rows();
        try (MemoryStack stack = MemoryStack.stackPush()){
            ByteBuffer buffer = stack.malloc(w * h);
            Framebuffer.bindRead(project.backBuffer());
            Framebuffer.readBuffer(channel.id);
            glPixelStorei(GL_PACK_ALIGNMENT,1);
            glReadPixels(0,0,w,h,GL_RED,GL_UNSIGNED_BYTE, buffer);
            pixels = new ByteArray2D(h,w);
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    pixels.set(buffer.get(w * r + c),c,r);
                }
            }
        }
    }
    
    
    @Override
    public int process(long queue_time_ms) throws Exception {
        if (visited.area().contains(origin)) {
            byte value_to_fill = pixels.get(origin.x,origin.y);
            if (value_to_fill != fillValue) {
                IntQueue searchQueue = new IntQueue(visited.area().size() / 2);
                searchQueue.enqueue(origin.x);
                searchQueue.enqueue(origin.y);
                int[][] adj = new int[][] {{-1, 0},{ 0,-1},{ 0, 1},{ 1, 0}};
                while (!searchQueue.isEmpty()) {
                    int cx = searchQueue.dequeue();
                    int cy = searchQueue.dequeue();
                    pixels.set(fillValue,cx,cy);
                    editArea.expandToContain(cx,cy);
                    for (int i = 0; i < 4; i++) {
                        int nx = cx + adj[i][0];
                        int ny = cy + adj[i][1];
                        if (visited.area().contains(nx,ny)) {
                            if (!visited.getUnsafe(nx,ny)) {
                                visited.setUnsafe(nx,ny);
                                byte value = pixels.get(nx,ny);
                                if (value == value_to_fill) {
                                    searchQueue.enqueue(nx);
                                    searchQueue.enqueue(ny);
                                }
                            }
                        }
                    }
                }
                return 1;
            }
        }
        return -1;
    }
    
    @Override
    public void onCompletion(Exception e, int status, long runtime_ms) {
        if (status == 1) {
            project.undoRedoManager().newEdit(editArea,channel, Brush.get());
            int x0 = editArea.minX();
            int y0 = editArea.minY();
            int w = editArea.cols();
            int h = editArea.rows();
            try (MemoryStack stack = MemoryStack.stackPush()){
                ByteBuffer buffer = stack.malloc(editArea.size());
                for (int r = 0; r < h; r++) {
                    for (int c = 0; c < w; c++) {
                        buffer.put(pixels.get(x0 + c, y0 + r));
                    }
                }
                Texture texture = project.backBuffer().texture(channel.id);
                texture.bindToActiveSlot();
                texture.uploadSubData(buffer.flip(),0,w,h,x0,y0);
            }
        } else if (e != null) Logger.warn(e,"while flood-filling");
    }
    
    
    
}
