package io.github.heathensoft.canvas;

import io.github.heathensoft.canvas.io.PngImporter;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.io.External;
import io.github.heathensoft.jlib.lwjgl.graphics.Color;
import io.github.heathensoft.jlib.lwjgl.graphics.Palette;
import io.github.heathensoft.jlib.lwjgl.utils.Input;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

/**
 * @author Frederik Dahl
 * 08/02/2023
 */


public class SpriteLighter extends Application {
    
    private static final int RESOLUTION_WIDTH = 1280;
    private static final int RESOLUTION_HEIGHT = 720;
    
    private Editor editor;
    private Palette palette;
    private OrthographicCamera testCam;
    
    protected void engine_init(List<Resolution> supported, BootConfiguration config, String[] args) {
        supported.add(new Resolution(RESOLUTION_WIDTH,RESOLUTION_HEIGHT));
        config.settings_width = RESOLUTION_WIDTH;
        config.settings_height = RESOLUTION_HEIGHT;
        config.windowed_mode = true;
        //config.auto_resolution = true;
        //config.limit_fps = false;
        //config.vsync_enabled = false;
    }
    
    protected void on_start(Resolution resolution) throws Exception {
        Input.initialize();
        
        testCam = new OrthographicCamera();
        testCam.viewport.set(resolution.width() / 2f, resolution.height());
        testCam.translateXY(resolution.width() / 2f, 0);
        testCam.refresh();
        
        System.out.println(resolution);
        editor = new Editor(resolution);
        External external = new External(External.USER_HOME("desktop","f"));
        
        Path colorPath = external.path().resolve("TTT3.png");
        PngImporter importer = new PngImporter();
        importer.importColorImage(colorPath);
        importer.importVolumeImage(external.path().resolve("TTT_volume.png"));
        importer.loadEmissiveImage(external.path().resolve("TTT3_emissive.png"));
        //importer.importDetailsImage(external.path().resolve("Female_1_details.png"));
        //importer.importSpecularImage(external.path().resolve("Female_1_specular.png"));
        
        if (!importer.status().ready) {
            System.out.println(importer.status().description);
            Engine.get().exit();
            return;
        }
        PngImporter.Textures textures = importer.generateTextures();
        importer.dispose();
        editor.newProject(textures);
        editor.lighting().setColor(new Color(0.85f,0.83f,0.75f,1f));
        editor.lighting().setAmbience(0.5f);
        List<String> lines = new Resources().asLines("palette/aerugo.hex");
        List<Color> colors = new ArrayList<>(lines.size());
        for (String line : lines) colors.add(Color.valueOf(line));
        palette = new Palette(colors,"aap-64",128);
        editor.setPalette(palette);
        editor.setDetailVolumeRatio(0.1f);
        editor.setDepthAmplitude(16f);
        editor.togglePreviewPalette();
    }
    
    
    protected void on_update(float delta) {
        //System.out.println(Engine.get().time().fps());
        Input input = Input.get();
        if (input.anyFilesDropped()) {
            input.collectDroppedFiles(System.out::println);
        }
        Keyboard keys = input.keyboard();
        Mouse mouse = input.mouse();
        if (keys.just_pressed(GLFW_KEY_ESCAPE)) {
            Engine.get().exit();
        }
        editor.process(mouse,keys);
        
        
        Vector2f mouseScreen = new Vector2f(mouse.position());
        mouseScreen.mul(RESOLUTION_WIDTH,RESOLUTION_HEIGHT);
        Vector3f m3 = new Vector3f(mouseScreen.x,mouseScreen.y,1.0f);
        m3.mulProject(testCam.combined());
        System.out.println(m3.x + " " + m3.y);
        
    }
    
    protected void on_render(float frame_time, float alpha) {
        editor.render(frame_time);
    }
    
    protected void on_exit() {
        
        
        Disposable.dispose(editor,palette);
    }
    
    protected void resolution_request(Resolution resolution) {}
    
    public static void main(String[] args) {
        Engine.get().run(new SpriteLighter(),args);
    }
}
