package io.github.heathensoft.canvas.old;

import io.github.heathensoft.canvas.CanvasGrid;
import io.github.heathensoft.canvas.TSpaceVTXBuffer;
import io.github.heathensoft.canvas.old.brushold.Channel;
import io.github.heathensoft.canvas.old.ioold.PngImporter;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.lwjgl.graphics.Framebuffer;
import io.github.heathensoft.jlib.lwjgl.graphics.ShaderProgram;
import io.github.heathensoft.jlib.lwjgl.graphics.Texture;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.List;

import static io.github.heathensoft.canvas.Shaders.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Frederik Dahl
 * 21/01/2023
 */


public class NormalizeMain extends Application {
    
    //---------------------------------------
    
    private ShaderProgram normalShader;
    private ShaderProgram passthroughShader;
    private boolean _switch;
    
    //---------------------------------------
    
    private static final int RESOLUTION_WIDTH = 1280;
    private static final int RESOLUTION_HEIGHT = 720;
    private SplitScreen splitScreen;
    private CanvasBackgroundOld canvasBackground;
    private UniformBatch uniformBatch;
    private CanvasGrid canvasGrid;
    private Vector4f mousePosition;
    private Vector4f textureBounds;
    private Project project;
    
    private float current_zoom;
    
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(new Resolution(RESOLUTION_WIDTH,RESOLUTION_HEIGHT));
        config.settings_width = RESOLUTION_WIDTH;
        config.settings_height = RESOLUTION_HEIGHT;
    }
    
    protected void on_start(Resolution resolution) throws Exception {
        
        splitScreen = new SplitScreen(RESOLUTION_WIDTH,RESOLUTION_HEIGHT);
        current_zoom = splitScreen.canvasCamera().zoom;
        canvasBackground = new CanvasBackgroundOld();
        uniformBatch = new UniformBatch(splitScreen.fullscreenCombined());
        canvasGrid = new CanvasGrid();
        mousePosition = new Vector4f();
        
        External external = new External(External.USER_HOME());
        Path texturePath = external.path().resolve("Tree.png");
        
        PngImporter importer = new PngImporter();
        importer.importColorImage(texturePath);
        if (!importer.status().ready) {
            System.out.println(importer.status().description);
            Engine.get().exit();
        }
        PngImporter.Textures textures = importer.generateTextures();
        importer.dispose();
        project = new Project(textures,0);
        textureBounds = new Vector4f(0,0,
                project.texturesWidth(),
                project.texturesHeight());
        
        
        //---------------------------------------------------------------
        
        Resources io = new Resources();
        normalShader = new ShaderProgram(
                io.asString(NORMAL_MAPPING_VERT_OLD),
                io.asString(NORMAL_MAPPING_FRAG_OLD));
        normalShader.createUniform(U_AMPLITUDE);
        normalShader.createUniform(U_SAMPLER_ARRAY);
    
        passthroughShader = new ShaderProgram(
                io.asString(TEXTURE_PASSTHROUGH_VERT_OLD),
                io.asString(TEXTURE_PASSTHROUGH_FRAG_OLD));
        passthroughShader.createUniform(U_SAMPLER_2D);
        
        TSpaceVTXBuffer.initialize();
    
        //---------------------------------------------------------------
        
        
        Input.initialize();
        glDisable(GL_DEPTH_TEST);
        
    }
    
    protected void on_update(float delta) {
        Keyboard keyboard = Input.get().keyboard();
        Mouse mouse = Input.get().mouse();
        OrthographicCamera camera = splitScreen.canvasCamera();
        
        if (keyboard.just_pressed(GLFW_KEY_ESCAPE)) Engine.get().exit();
        if (keyboard.just_pressed(GLFW_KEY_KP_ADD)) canvasGrid.incrementSize();
        if (keyboard.just_pressed(GLFW_KEY_KP_SUBTRACT)) canvasGrid.decrementSize();
        if (keyboard.just_pressed(GLFW_KEY_L)) _switch = !_switch;
    
        if (mouse.scrolled()) {
            float amount = mouse.get_scroll();
            current_zoom -= amount;
            int pow = (int) current_zoom;
            camera.zoom = (float) Math.pow(2,pow);
            camera.refresh();
            System.out.println("bounds height: " + (camera.bounds.lengthY()));
            System.out.println("zoom: " + camera.zoom);
        }
    
        if (mouse.is_dragging(Mouse.WHEEL)) {
            Vector2f drag_vec = mouse.delta_vector();
            camera.position.x -= drag_vec.x * 1280 * camera.zoom;
            camera.position.y -= drag_vec.y * 720 * camera.zoom;
        }
        
        Vector2f dest = splitScreen.unProjectMouse(mouse.ndc(),MathLib.vec2());
        mousePosition.set(dest.x,dest.y,0,0);
    }
    
    protected void on_render(float frame_time, float alpha) {
        
        splitScreen.canvasCamera().refresh();
        uniformBatch.upload(
                splitScreen.canvasCamera(),
                textureBounds,
                mousePosition
        );
    
        Texture depth_texture = project.frontBuffer().texture(Channel.DEPTH.idx);
        Texture color_texture = project.colorSourceTexture();
        Texture normal_texture = project.normalsBuffer().texture(0);
        Texture preview_texture = project.previewBuffer().texture(0);
        
        // texture pipeline here: --------------------------------
        
        project.viewport();
        Framebuffer.bindDraw(project.normalsBuffer());
        Framebuffer.clear();
        
        // draw to normal texture. in: depth, color
        
        float amplitude = 4.0f;
        
        glDisable(GL_BLEND);
        normalShader.use();
        normalShader.setUniform1f(U_AMPLITUDE,amplitude);
        
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer buffer = stack.mallocInt(2);
            buffer.put(0).put(1).flip();
            normalShader.setUniform1iv(U_SAMPLER_ARRAY,buffer);
            depth_texture.bindToSlot(0);
            color_texture.bindToSlot(1);
        }
        
        TSpaceVTXBuffer.transferElements();
        
        // draw to preview texture. in: normal
        
        Framebuffer.bindDraw(project.previewBuffer());
        Framebuffer.clear();
        passthroughShader.use();
        passthroughShader.setUniform1i(U_SAMPLER_2D,0);
        normal_texture.bindToSlot(0);
        TSpaceVTXBuffer.transferElements();
        
        
        // -------------------------------------------------------
        
        splitScreen.bindFramebufferDraw();
        splitScreen.setDrawBuffersBoth();
        Framebuffer.viewport();
        Framebuffer.clear();
        splitScreen.setDrawBufferLeft();
        canvasBackground.draw();
        splitScreen.setDrawBuffersBoth();
        
        splitScreen.drawToSplitScreen(
                depth_texture,
                _switch ? color_texture : preview_texture,
                color_texture
        );
        splitScreen.setDrawBufferLeft();
        canvasGrid.draw(splitScreen.canvasCamera());
        Framebuffer.bindDefault();
        Framebuffer.viewport();
        Framebuffer.clear();
        splitScreen.drawFromSplitScreen();
        
    }
    
    protected void on_exit() {
        
        //--------------------------------------------------------
        
        TSpaceVTXBuffer.dispose();
        Disposable.dispose(
                passthroughShader,
                normalShader
        );
        //--------------------------------------------------------
       
        Disposable.dispose(
                canvasBackground,
                canvasGrid,
                uniformBatch,
                splitScreen,
                project
        );
    }
    
    protected void resolution_request(Resolution resolution) throws Exception { /* */ }
    
    public static void main(String[] args) {
        Engine.get().run(new NormalizeMain(),args);
    }
}
