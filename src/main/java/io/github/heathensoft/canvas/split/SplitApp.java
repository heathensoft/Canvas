package io.github.heathensoft.canvas.split;

import io.github.heathensoft.canvas.neo.SplitScreen;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * @author Frederik Dahl
 * 23/02/2023
 */


public class SplitApp extends Application {
    
    public static void main(String[] args) {
        Engine.get().run(new SplitApp(),args);
    }
    
    Renderer renderer;
    
    @Override
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(Resolution.R_1280x720);
        config.settings_height = 720;
        config.settings_width = 1280;
        config.windowed_mode = true;
    }
    
    @Override
    protected void on_start(Resolution resolution) throws Exception {
        Input.initialize();
        renderer = new Renderer(resolution);
    }
    
    @Override
    protected void on_update(float delta) {
        SplitScreen splitScreen = renderer.splitScreen;
        Keyboard keys = Input.get().keyboard();
        if (keys.pressed(GLFW.GLFW_KEY_A)) {
            splitScreen.adjustDividingLine(delta);
        }
        if (keys.pressed(GLFW.GLFW_KEY_D)) {
            splitScreen.adjustDividingLine(-delta);
        }
    }
    
    @Override
    protected void on_render(float frame_time, float alpha) {
        renderer.render();
    }
    
    @Override
    protected void on_exit() {
        renderer.dispose();
    }
    
    @Override
    protected void resolution_request(Resolution resolution) throws Exception {
    
    }
}
