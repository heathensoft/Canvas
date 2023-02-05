package io.github.heathensoft.canvas;

import io.github.heathensoft.canvas.f.Project;
import io.github.heathensoft.canvas.f.io.PngImporter;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector2f;

import java.nio.file.Path;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;

/**
 * @author Frederik Dahl
 * 26/01/2023
 */


public class TestApp extends Application {
    
    private static final int RESOLUTION_WIDTH = 1280;
    private static final int RESOLUTION_HEIGHT = 720;
    
    private float current_zoom;
    private Vector2f mouse_position;
    private RenderTest renderer;
    
    
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(new Resolution(RESOLUTION_WIDTH,RESOLUTION_HEIGHT));
        config.settings_width = RESOLUTION_WIDTH;
        config.settings_height = RESOLUTION_HEIGHT;
    }
    
    protected void on_start(Resolution resolution) throws Exception {
        SplitScreen splitScreen = new SplitScreen(RESOLUTION_WIDTH,RESOLUTION_HEIGHT);
        current_zoom = splitScreen.camera().zoom;
        mouse_position = new Vector2f();
    
        External external = new External(External.USER_HOME("desktop"));
        Path texturePath = external.path().resolve("rock2.png");
    
        PngImporter importer = new PngImporter();
        importer.importColorImage(texturePath);
        if (!importer.status().ready) {
            System.out.println(importer.status().description);
            Engine.get().exit();
            return;
        }
        PngImporter.Textures textures = importer.generateTextures();
        importer.dispose();
        Project project = new Project(textures,0);
        renderer = new RenderTest(splitScreen,project);
        Input.initialize();
        glDisable(GL_DEPTH_TEST);
        
    }
    
    protected void on_update(float delta) {
        
        Keyboard keyboard = Input.get().keyboard();
        Mouse mouse = Input.get().mouse();
        SplitScreen splitScreen = renderer.splitScreen;
        CanvasGrid canvasGrid = renderer.canvasGrid;
        OrthographicCamera camera = splitScreen.camera();
    
        if (keyboard.just_pressed(GLFW_KEY_ESCAPE)) Engine.get().exit();
        if (keyboard.just_pressed(GLFW_KEY_KP_ADD)) canvasGrid.incrementSize();
        if (keyboard.just_pressed(GLFW_KEY_KP_SUBTRACT)) canvasGrid.decrementSize();
    
        if (mouse.scrolled()) {
            
            float amount = mouse.get_scroll();
            if (keyboard.pressed(GLFW_KEY_LEFT_CONTROL)) {
                renderer.light.position().z -= (amount * 5.0f);
                if (renderer.light.position().z < renderer.depth_amplitude) {
                    renderer.light.position().z = renderer.depth_amplitude;
                }
            } else {
                current_zoom -= amount;
                System.out.println("current zoom: " + current_zoom);
                int pow = (int) current_zoom;
                camera.zoom = (float) Math.pow(2,pow);
                System.out.println("camera zoom: " + camera.zoom);
                System.out.println(camera.zoom * 512);
            }
            
        }
    
        if (mouse.is_dragging(Mouse.WHEEL)) {
            Vector2f drag_vec = mouse.delta_vector();
            camera.position.x -= drag_vec.x * 1280 * camera.zoom;
            camera.position.y -= drag_vec.y * 720 * camera.zoom;
        }
    
        Vector2f dest = splitScreen.unProjectMouse(mouse.ndc(), MathLib.vec2());
        mouse_position.set(dest.x,dest.y);
        
        if(mouse.button_pressed(Mouse.RIGHT)) {
            float z = renderer.light.position().z;
            renderer.light.position().set(mouse_position.x,mouse_position.y,z);
        }
    
        if (keyboard.just_pressed(GLFW_KEY_L)) {
        
        
        }
        
        if (keyboard.just_pressed(GLFW_KEY_S)) {
            float z = renderer.light.position().z;
            renderer.light.position().z = Math.max(20,z-10);
            
        }if (keyboard.just_pressed(GLFW_KEY_W)) {
            float z = renderer.light.position().z;
            renderer.light.position().z = Math.min(500,z+10);
        }
        
    }
    
    protected void on_render(float frame_time, float alpha) {
        renderer.render(mouse_position,frame_time);
    }
    
    protected void on_exit() {
        /*
        try {
            renderer.project.save(External.USER_HOME("desktop","f"),true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
         */
        Disposable.dispose(renderer);
    }
    
    protected void resolution_request(Resolution resolution) throws Exception {
    
    }
    
    public static void main(String[] args) {
        Engine.get().run(new TestApp(),args);
    }
}
