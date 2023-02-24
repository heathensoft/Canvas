package io.github.heathensoft.canvas.neo;

import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import io.github.heathensoft.jlib.lwjgl.window.Viewport;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.glScissor;

/**
 * @author Frederik Dahl
 * 22/02/2023
 */


public class SplitScreen {
    
    private final Resolution resolution;
    private final Matrix4f viewShared;
    private final Matrix4f projectionLeft;
    private final Matrix4f projectionRight;
    private final Matrix4f unprojectNDCLeft;
    private final Matrix4f unprojectNDCRight;
    private final Matrix4f combinedLeft;
    private final Matrix4f combinedRight;
    private final Vector3f position;
    private final Vector3f direction;
    private final float far  = 1.0f;
    private final float near = 0.01f;
    private float dividingLine = 0.5f;
    private float zoom = 1.0f;
    
    
    public SplitScreen(Resolution resolution) {
        this.resolution = resolution;
        int res_height = resolution.height();
        int res_width  = resolution.width();
        int left_width = (int)(res_width * dividingLine);
        int right_width = res_width - left_width;
        position = new Vector3f(0,0,1);
        direction = new Vector3f(0,0,-1);
        viewShared = new Matrix4f().lookAt(position, direction,MathLib.UP_VECTOR);
        float lr = left_width / 2f * zoom;
        float tb = res_height / 2f * zoom;
        projectionLeft = new Matrix4f().ortho(-lr,lr,-tb,tb,near,far);
        combinedLeft = new Matrix4f(projectionLeft).mul(viewShared);
        lr = right_width / 2f * zoom;
        tb = res_height / 2f * zoom;
        projectionRight= new Matrix4f().ortho(-lr,lr,-tb,tb,near,far);
        combinedRight = new Matrix4f(projectionRight).mul(viewShared);
        unprojectNDCLeft = new Matrix4f(
                dividingLine, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                (dividingLine - 1), 0.0f, 0.0f, 1.0f).invert();
        unprojectNDCRight = new Matrix4f(
                (1 - dividingLine), 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                dividingLine, 0.0f, 0.0f, 1.0f).invert();
    }
    
    
    
    public void refresh() {
        direction.set(position.x,position.y,direction.z);
        viewShared.identity().lookAt(position,direction,MathLib.UP_VECTOR);
        int res_height = resolution.height();
        int res_width  = resolution.width();
        int left_width = (int)(res_width * dividingLine);
        int right_width = res_width - left_width;
        float lr = left_width / 2f * zoom;
        float tb = res_height / 2f * zoom;
        projectionLeft.identity().ortho(-lr,lr,-tb,tb,near,far);
        combinedLeft.set(projectionLeft).mul(viewShared);
        lr = right_width / 2f * zoom;
        tb = res_height / 2f * zoom;
        projectionRight.identity().ortho(-lr,lr,-tb,tb,near,far);
        combinedRight.set(projectionRight).mul(viewShared);
        unprojectNDCLeft.set(
                dividingLine, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                (dividingLine - 1), 0.0f, 0.0f, 1.0f).invert();
        unprojectNDCRight.set(
                (1 - dividingLine), 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                dividingLine, 0.0f, 0.0f, 1.0f).invert();
        
    }
    
    public boolean mouseOnLeftSide(Vector2f mouseNDC) {
        int width_left = leftScreenWidth();
        if (width_left == resolution.width()) return true;
        else if (width_left == 0) return false;
        return (mouseNDC.x + 1.0f) / 2.0f <= dividingLine;
    }
    
    public boolean mouseOnRightSide(Vector2f mouseNDC) {
        return !mouseOnLeftSide(mouseNDC);
    }
    
    public void unprojectMouseLeft(Vector2f mouseNDC, Vector2f dest) {
        Vector3f v3 = MathLib.vec3(mouseNDC.x,mouseNDC.y,0);
        v3.mulProject(unprojectNDCLeft).mulProject(leftCombinedINV(MathLib.mat4()));
        dest.set(v3.x,v3.y);
    }
    
    public void unprojectMouseRight(Vector2f mouseNDC, Vector2f dest) {
        Vector3f v3 = MathLib.vec3(mouseNDC.x,mouseNDC.y,0);
        v3.mulProject(unprojectNDCRight).mulProject(rightCombinedINV(MathLib.mat4()));
        dest.set(v3.x,v3.y);
    }
    
    public void unprojectMouse(Vector2f mouseNDC, Vector2f dest) {
        if (mouseOnLeftSide(mouseNDC)) unprojectMouseLeft(mouseNDC,dest);
        else unprojectMouseRight(mouseNDC,dest);
    }
    
    public void viewportLeft() {
        Viewport viewport = Engine.get().window().viewport();
        viewport.set(0,0,leftScreenWidth(),screenHeight());
    }
    
    public void viewportRight() {
        Viewport viewport = Engine.get().window().viewport();
        viewport.set(leftScreenWidth(),0,rightScreenWidth(),screenHeight());
    }
    
    public void scissorLeft() {
        int h = resolution.height();
        int w = leftScreenWidth();
        glScissor(0,0,w,h);
    }
    
    public void scissorRight() {
        int x = leftScreenWidth();
        int h = resolution.height();
        int w = resolution.width() - x;
        glScissor(x,0,w,h);
    }
    
    public void scissorRestore() {
        glScissor(0,0,resolution.width(),resolution.height());
    }
    
    public void setDividingLine(float dividingLine) {
        this.dividingLine = Math.max(0.0f,Math.min(1.0f,dividingLine));
    }
    
    public void adjustDividingLine(float amount) {
        setDividingLine(dividingLine + amount);
    }
    
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }
    
    public void setPosition(Vector2f position) {
        setPosition(position.x,position.y);
    }
    
    public void setPosition(float x, float y) {
        position.set(x,y,1);
    }
    
    public void translate(Vector2f translation) {
        translate(translation.x,translation.y);
    }
    
    public void translate(float x, float y) {
        position.add(x,y,0);
    }
    
    public int screenHeight() {
        return resolution.height();
    }
    
    public int leftScreenWidth() {
        return (int) (dividingLine * resolution.width());
    }
    
    public int rightScreenWidth() {
        return resolution.width() - leftScreenWidth();
    }
    
    public Resolution resolutionCombined() {
        return resolution;
    }
    
    public Matrix4f leftCombined() {
        return combinedLeft;
    }
    
    public Matrix4f leftCombinedINV(Matrix4f dest) {
        return dest.set(combinedLeft).invert();
    }
    
    public Matrix4f rightCombined() {
        return combinedRight;
    }
    
    public Matrix4f rightCombinedINV(Matrix4f dest) {
        return dest.set(combinedRight).invert();
    }
    
    public Vector3f position() {
        return position;
    }
    
    public float dividingLine() {
        return dividingLine;
    }
    
    public float zoom() {
        return zoom;
    }
}
