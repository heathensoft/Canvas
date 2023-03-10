package io.github.heathensoft.canvas;


import io.github.heathensoft.canvas.io.PngImporter;
import io.github.heathensoft.canvas.neo.FloodFillTask;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.generic.Container;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.common.utils.DiscreteLine;
import io.github.heathensoft.jlib.common.utils.IDPool;
import io.github.heathensoft.jlib.lwjgl.graphics.BufferObject;
import io.github.heathensoft.jlib.lwjgl.graphics.Palette;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.window.Engine;
import io.github.heathensoft.jlib.lwjgl.window.Keyboard;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.*;
import org.joml.Math;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.nio.FloatBuffer;
import java.util.*;

import static io.github.heathensoft.canvas.ENUM.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

/**
 * @author Frederik Dahl
 * 04/02/2023
 */


public class Editor implements Disposable {
    
    public static final int EDITOR_BINDING_POINT = 1;
    public static final int PROJECT_BINDING_POINT = 2;
    public static final int EDITOR_UNIFORMS_SIZE_FLOAT = 44;
    public static final int PROJECT_UNIFORMS_SIZE_FLOAT = 36;
    public static final float DETAIL_TO_VOLUME_RATIO_DEFAULT = 0.1f;
    public static final float DEPTH_AMPLITUDE_DEFAULT = 8.0f;
    public static final float DEPTH_AMPLITUDE_MIN = 1.0f;
    public static final float DEPTH_AMPLITUDE_MAX = 16.0f;
    public static final float LIGHT_Z_SCROLL_DELTA = 8.0f;
    public static final float LIGHT_Z_POSITION_MAX = 1000.0f;
    public static final float CAMERA_ZOOM_MAX =  2.0f; // 0 = 100%
    public static final float CAMERA_ZOOM_MIN = -5.0f;
    
    private final IDPool projectIdPool;
    private final Container<Project> projectsById;
    private final List<Project> projectList;
    
    private final BufferObject uniformBufferEditor;
    private final BufferObject uniformBufferProject;
    private final EditorGraphics graphics;
    
    private final Brush brush;
    private final Palette defaultPalette;
    private final PreviewLighting lighting;
    private Project activeProject;
    private Palette currentPalette;
    private Channel currentChannel;
    private PreviewDisplay previewDisplay;
    
    protected final Area brushDragArea;
    protected final Area editableAreaBounds;
    protected final DiscreteLine lineDrawCoordinates;
    protected final Set<Coordinate> freeHandCoordinates;
    protected final Coordinate mouseCoordCurrent;
    protected final Coordinate mouseCoordStart;
    private final OrthographicCamera camera;
    private final Matrix4f unprojectMatLeft;
    private final Matrix4f unprojectMatRight;
    private final Vector2f mouseCurrent;
    private final Vector2f mouseStart;
    private final int screen_width;
    private final int screen_height;
    
    private float detail_to_volume_ratio;
    private float virtual_depth_amplitude;
    private float shader_time_accumulator;
    private float current_zoom_amount;
    private boolean currently_dragging;
    private boolean currently_editing;
    private boolean preview_lighting;
    private boolean preview_shadow;
    private boolean preview_palette;
    
    
    public Editor(Resolution resolution) throws Exception {
        this.projectIdPool = new IDPool();
        this.projectsById = new Container<>(8);
        this.projectList = new ArrayList<>(8);
        this.lineDrawCoordinates = new DiscreteLine(0,0,0,0);
        this.freeHandCoordinates = new HashSet<>(137); // arbitrary prime
        this.brushDragArea = new Area();
        this.editableAreaBounds = new Area();
        this.mouseCurrent = new Vector2f();
        this.mouseStart = new Vector2f();
        this.mouseCoordCurrent = new Coordinate();
        this.mouseCoordStart = new Coordinate();
        this.screen_width = resolution.width();
        this.screen_height = resolution.height();
        this.unprojectMatLeft = new Matrix4f(
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.0f, 0.0f, 1.0f).invert();
        this.unprojectMatRight = new Matrix4f(
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.0f, 0.0f, 1.0f).invert();
        this.camera = new OrthographicCamera();
        this.camera.viewport.set(screen_width / 2f, screen_height);
        this.camera.refresh();

        this.currentChannel = Channel.DEFAULT;
        this.previewDisplay = PreviewDisplay.DEFAULT;
        this.virtual_depth_amplitude = DEPTH_AMPLITUDE_DEFAULT;
        this.detail_to_volume_ratio = DETAIL_TO_VOLUME_RATIO_DEFAULT;
        this.current_zoom_amount = 0.0f;
        this.brush = Brush.get();
        this.lighting = PreviewLighting.get();
        this.defaultPalette = Palette.brightFuture();
        this.currentPalette = defaultPalette;
        this.uniformBufferEditor = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW).bind();
        this.uniformBufferEditor.bufferData((long) EDITOR_UNIFORMS_SIZE_FLOAT * Float.BYTES);
        this.uniformBufferEditor.bindBufferBase(EDITOR_BINDING_POINT);
        this.uniformBufferProject = new BufferObject(GL_UNIFORM_BUFFER,GL_DYNAMIC_DRAW).bind();
        this.uniformBufferProject.bufferData((long) PROJECT_UNIFORMS_SIZE_FLOAT * Float.BYTES);
        this.uniformBufferProject.bindBufferBase(PROJECT_BINDING_POINT);
        this.graphics = new EditorGraphics(this);
        this.preview_lighting = true;
        this.preview_palette = true;
        this.preview_shadow = true;
        Shaders.initialize();
    }
    
    
    public void render(float frame_time) {
        camera.refresh();
        shader_time_accumulator += frame_time;
        lighting.uploadUniformBlock();
        brush.uploadUniformBlock();
        uploadEditorUniformBlock();
        if (projectIsOpen()) {
            uploadProjectUniformBlock();
            graphics.projectPipeline(activeProject);
        } graphics.drawToScreen();
        
    }
    
    public void process(Mouse mouse, Keyboard keys) {
        unProjectMouse(mouse.ndc(),mouseCurrent);
        mouseCoordCurrent.set(
                (int) mouseCurrent.x,
                (int) mouseCurrent.y);
        if (!currently_editing) {
            lineDrawCoordinates.set(
                    mouseCoordCurrent,
                    mouseCoordCurrent);
        } currently_dragging = false;
        processInput(mouse,keys);
        if (projectIsOpen()) {
            processEditing(mouse);
        } else currently_editing = false;
    }
    
    private void processEditing(Mouse mouse) {
        
        if (brush().tool() == BrushTool.SAMPLER) { // Sampling is not editing
            currently_editing = false;
            currently_dragging = false;
            if (activeProject.withinBounds(mouseCoordCurrent)) {
                brushDragArea.set(mouseCoordCurrent);
                int value = activeProject.readPixel(mouseCoordCurrent,currentChannel);
                
                if (mouse.button_pressed(Mouse.LEFT)) {
                    /*
                    if (mouse.just_clicked(Mouse.LEFT)) {
                        if (value != brush.colorValue()) {
                            Engine.get().threadPool().handle(
                                    new FloodFillTask(activeProject,currentChannel,
                                            mouseCoordCurrent,(byte) (brush.colorValue() & 0xFF)));
                        }
                    }
                    
                     */
                    brush.setColor(value);
                    System.out.println(value);
                }
            }
        } else {
            if (currently_editing) {
                if (!mouse.button_pressed(Mouse.LEFT)) { // FINISH EDITING
                    currently_editing = false;
                    UndoRedoManager URM = activeProject.undoRedoManager();
                    Area projectArea = activeProject.area();
                    switch (brush.tool()) {
                        case FREE_HAND -> {
                            if (!freeHandCoordinates.isEmpty()) {
                                int min_x = Integer.MAX_VALUE;
                                int min_y = Integer.MAX_VALUE;
                                int max_x = Integer.MIN_VALUE;
                                int max_y = Integer.MIN_VALUE;
                                for(Coordinate c : freeHandCoordinates) {
                                    min_x = java.lang.Math.min(min_x,c.x);
                                    min_y = java.lang.Math.min(min_y,c.y);
                                    max_x = java.lang.Math.max(max_x,c.x);
                                    max_y = java.lang.Math.max(max_y,c.y);
                                } Area editArea = new Area(min_x,min_y,max_x,max_y);
                                editArea.expand(brush.brushSize() / 2);
                                if (projectArea.intersection(editArea)) {
                                    URM.newEdit(editArea,currentChannel,brush);
                                    graphics.drawToBackbuffer(currentChannel);
                                }
                            }
                        }
                        case LINE_DRAW -> {
                            Area editArea = new Area(
                                    lineDrawCoordinates.p0(),
                                    lineDrawCoordinates.p1());
                            editArea.expand(brush.brushSize() / 2);
                            if (projectArea.intersection(editArea)) {
                                URM.newEdit(editArea,currentChannel,brush);
                                graphics.drawToBackbuffer(currentChannel);
                            }
                        }
                        case DRAG_AREA -> {
                            Area editArea = new Area(brushDragArea);
                            if (projectArea.intersection(editArea)) {
                                URM.newEdit(editArea,currentChannel,brush);
                                graphics.drawToBackbuffer(currentChannel);
                            }
                        }
                        default -> {}
                    }
                } else { // CONTINUE EDITING
                    currently_dragging = true;
                    switch (brush.tool()) {
                        case FREE_HAND -> {
                            if (editableAreaBounds.contains(mouseCoordCurrent)) {
                                freeHandCoordinates.add(new Coordinate(mouseCoordCurrent));
                            }
                        }
                        case LINE_DRAW -> lineDrawCoordinates.set(mouseCoordStart,mouseCoordCurrent);
                        case DRAG_AREA -> brushDragArea.set( mouseCoordStart, mouseCoordCurrent);
                        default -> {}
                    }
                }
            } else {
                if (mouse.just_started_drag(Mouse.LEFT)) {
                    if (editableAreaBounds.contains(mouseCoordCurrent)) { // START EDITING
                        currently_dragging = true;
                        currently_editing = true;
                        freeHandCoordinates.clear();
                        mouseStart.set(mouseCurrent);
                        mouseCoordStart.set(mouseCoordCurrent);
                        brushDragArea.set(mouseCoordStart);
                    }
                }
            }
        }
    }
    
    private void processInput(Mouse mouse, Keyboard keys) {
        final boolean PROJECT_OPEN = projectIsOpen();
        final boolean CTRL = keys.pressed(GLFW_KEY_LEFT_CONTROL) || keys.pressed(GLFW_KEY_RIGHT_CONTROL);
        final boolean SHIFT = keys.pressed(GLFW_KEY_LEFT_SHIFT) || keys.pressed(GLFW_KEY_RIGHT_SHIFT);
        final boolean ALT = keys.pressed(GLFW_KEY_LEFT_ALT) || keys.pressed(GLFW_KEY_RIGHT_ALT);
        final boolean SCROLLED = mouse.scrolled();
        
        if (SCROLLED) {
            float amount = mouse.get_scroll();
            if (mouse.is_dragging(Mouse.RIGHT)) {
                float z = lighting.position().z;
                lighting.position().z = Math.clamp(
                        virtual_depth_amplitude,
                        LIGHT_Z_POSITION_MAX,
                        z - (amount * LIGHT_Z_SCROLL_DELTA));
            } else {
                float zoom_raw = current_zoom_amount - amount;
                zoom_raw = Math.clamp(CAMERA_ZOOM_MIN, CAMERA_ZOOM_MAX, zoom_raw);
                if (current_zoom_amount != zoom_raw) {
                    current_zoom_amount = zoom_raw;
                    System.out.println(zoom_raw);
                    int pow = (int) current_zoom_amount;
                    camera.zoom = (float) java.lang.Math.pow(2,pow);
                }
            }
        }
        if (mouse.is_dragging(Mouse.WHEEL)) {
            currently_dragging = true;
            Vector2f drag_vec = mouse.delta_vector();
            camera.position.x -= drag_vec.x * screen_width * camera.zoom;
            camera.position.y -= drag_vec.y * screen_height * camera.zoom;
        }
        if (mouse.is_dragging(Mouse.RIGHT)) {
            currently_dragging = true;
            lighting.setPosition(mouseCurrent);
        }
        
        if (CTRL && SHIFT) {
            if (keys.just_pressed(GLFW_KEY_Z) && PROJECT_OPEN) {
                activeProject.undoRedoManager().redo();
            } else if (keys.just_pressed(GLFW_KEY_S) && PROJECT_OPEN) {
                try { activeProject.saveAll(true);
                } catch (Exception e) {
                    Logger.error(e,"unable to save");
                }
            }
        } else if (CTRL && ALT) {
        
        } else if (CTRL) {
            if (PROJECT_OPEN) {
                try {
                    if (keys.just_pressed(GLFW_KEY_Z)) {
                        activeProject.undoRedoManager().undo();
                    } else if (keys.just_pressed(GLFW_KEY_Y)) {
                        activeProject.undoRedoManager().redo();
                    } else if (keys.just_pressed(GLFW_KEY_S)) {
                        activeProject.save(currentChannel,true);
                    } else if (keys.just_pressed(GLFW_KEY_N)) {
                        activeProject.saveNormals(true);
                    } else if (keys.just_pressed(GLFW_KEY_D)) {
                        activeProject.saveDepth(true);
                    } else if (keys.just_pressed(GLFW_KEY_P)) {
                        activeProject.savePreview(true);
                    } else if (keys.just_pressed(GLFW_KEY_A)) {
                        activeProject.saveAll(true);
                    }
                } catch (Exception e) {
                    Logger.error(e,"unable to save, undo or redo");
                }
            }
            if (keys.just_pressed(GLFW_KEY_PAGE_UP)) {
                setDepthAmplitude(virtual_depth_amplitude + 1);
                System.out.println("depth amplitude: " + virtual_depth_amplitude);
            } else if (keys.just_pressed(GLFW_KEY_PAGE_DOWN)) {
                setDepthAmplitude(virtual_depth_amplitude - 1);
                System.out.println("depth amplitude: " + virtual_depth_amplitude);
            } else if (keys.just_pressed(GLFW_KEY_UP)) {
                brush.incrementBrushSize(1);
            } else if (keys.just_pressed(GLFW_KEY_DOWN)) {
                brush.decrementBrushSize(1);
            } else if (keys.just_pressed(GLFW_KEY_KP_ADD)) {
                brush.incrementColor(16);
            } else if (keys.just_pressed(GLFW_KEY_KP_SUBTRACT)) {
                brush.decrementColor(16);
            }  else { int i = 1;
                for (Project project : projectList) {
                    if (i == 10) break;
                    if (keys.just_pressed(GLFW_KEY_0 + i)) {
                        openProject(project.projectID());
                    } i++;
                }
            }
            
        } else if (SHIFT) {
            if (keys.just_pressed(GLFW_KEY_UP)) {
                brush.setBrushSize(Brush.TEXTURE_SIZE);
            } else if (keys.just_pressed(GLFW_KEY_DOWN)) {
                brush.setBrushSize(1);
            }
        } else if (ALT) {
            if (keys.just_pressed(GLFW_KEY_L)) {
                togglePreviewLighting();
            } else if (keys.just_pressed(GLFW_KEY_T)) {
                lighting.togglePointLight();
            } else if (keys.just_pressed(GLFW_KEY_S)) {
                togglePreviewShadow();
            } else if (keys.just_pressed(GLFW_KEY_P)) {
                togglePreviewPalette();
            } else if (keys.just_pressed(GLFW_KEY_UP)) {
                lighting.increaseBrightness();
            } else if (keys.just_pressed(GLFW_KEY_DOWN)) {
                lighting.decreaseBrightness();
            }
        } else {
            if (keys.just_pressed(GLFW_KEY_UP)) {
                previewDisplay = previewDisplay.prev();
            } else if (keys.just_pressed(GLFW_KEY_DOWN)) {
                previewDisplay = previewDisplay.next();
            } else if (keys.just_pressed(GLFW_KEY_LEFT)) {
                currentChannel = currentChannel.prev();
            } else if (keys.just_pressed(GLFW_KEY_RIGHT)) {
                currentChannel = currentChannel.next();
            } else if (keys.just_pressed(GLFW_KEY_PAGE_UP)) {
                setDetailVolumeRatio(detail_to_volume_ratio + 0.1f);
                System.out.println("detail / volume: " + detail_to_volume_ratio);
            } else if (keys.just_pressed(GLFW_KEY_PAGE_DOWN)) {
                setDetailVolumeRatio(detail_to_volume_ratio - 0.1f);
                System.out.println("detail / volume: " + detail_to_volume_ratio);
            } else if (keys.just_pressed(GLFW_KEY_BACKSLASH)) {
                graphics.grid().toggleHide();
            } else if (keys.just_pressed(GLFW_KEY_KP_MULTIPLY)) {
                graphics.grid().incrementSize();
            } else if (keys.just_pressed(GLFW_KEY_KP_DIVIDE)) {
                graphics.grid().decrementSize();
            } else if (keys.just_pressed(GLFW_KEY_S)) {
                brush.toggleShape();
            } else if (keys.just_pressed(GLFW_KEY_KP_ADD)) {
                brush.incrementColor(1);
            } else if (keys.just_pressed(GLFW_KEY_KP_SUBTRACT)) {
                brush.decrementColor(1);
            } else if (keys.just_pressed(GLFW_KEY_I)) {
                brush.setTool(BrushTool.SAMPLER);
            } else if (keys.just_pressed(GLFW_KEY_L)) {
                if (brush.tool() == BrushTool.LINE_DRAW) {
                    brush.toggleShape();
                } else brush.setTool(BrushTool.LINE_DRAW);
            } else if (keys.just_pressed(GLFW_KEY_B)) {
                if (brush.tool() == BrushTool.FREE_HAND) {
                    brush.toggleShape();
                } else brush.setTool(BrushTool.FREE_HAND);
            } else if (keys.just_pressed(GLFW_KEY_U)) {
                if (brush.tool() == BrushTool.DRAG_AREA) {
                    brush.toggleShape();
                } else brush.setTool(BrushTool.DRAG_AREA);
            } else {
                for (int i = 0; i < BrushFunction.SIZE; i++) {
                    if (keys.just_pressed(GLFW_KEY_0 + i)) {
                        brush.setFunction(BrushFunction.ALL[i]);
                    }
                }
            }
        }
    }
    
    public int newProject(PngImporter.Textures textures) {
        int id = projectIdPool.obtainID();
        Project project = null;
        try { project = new Project(textures,id);
            projectsById.set(project.projectID(),project);
            projectList.add(project);
            projectList.sort(Comparator.naturalOrder());
            openProject(id);
        } catch (Exception e) {
            projectIdPool.returnID(id);
            Disposable.dispose(project);
            Logger.error(e,"failed to create project");
            return -1;
        } return id;
    }
    
    public void openProject(int project_id) {
        Project project = getProject(project_id);
        if (project != null) {
            setActiveProject(project);
        }
    }
    
    public Project getProject(int project_id) {
        if (projectsById.capacity() - 1 >= project_id) {
            return projectsById.get(project_id);
        } return null;
    }
    
    public List<Project> getProjects() {
        return projectList;
    }
    
    private void setActiveProject(Project project) {
        if (project != null) {
            editableAreaBounds.set(project.area());
            editableAreaBounds.expand(brush.textureSize() / 2);
        } activeProject = project;
    }
    
    public Project activeProject() {
        if (projectIsOpen()) {
            return activeProject;
        } return null;
    }
    
    public void closeProject(int project_id) {
        Project project = getProject(project_id);
        if (project != null) {
            if (activeProject == project) {
                setActiveProject(null);
            } projectsById.set(project_id,null);
            projectIdPool.returnID(project_id);
            projectList.remove(project);
            if (!projectList.isEmpty()) {
                projectList.sort(Comparator.naturalOrder());
            } project.dispose();
        }
    }
    
    public void closeActiveProject() {
        if (activeProject != null) {
            closeProject(activeProject.projectID());
        }
    }
    
    public void closeAllProjects() {
        for (Project project : projectList) {
            int project_id = project.projectID();
            if (activeProject == project) {
                setActiveProject(null);
            } projectsById.set(project_id,null);
            projectIdPool.returnID(project_id);
            project.dispose();
        } projectList.clear();
    }
    
    public void unProjectMouse(Vector2f mouseNDC, Vector2f dest) {
        Vector3f v3 = MathLib.vec3(mouseNDC.x,mouseNDC.y,0);
        if (v3.x > 0) v3.mulProject(unprojectMatRight);
        else v3.mulProject(unprojectMatLeft);
        dest.set(v3.x,v3.y);
        camera.unProject(dest);
    }
    
    public void setDetailVolumeRatio(float ratio) {
        detail_to_volume_ratio = Math.clamp(0.0f,1.0f,ratio);
    }
    
    public void setDepthAmplitude(float amplitude) {
        amplitude = Math.abs(amplitude);
        amplitude = Math.clamp(DEPTH_AMPLITUDE_MIN,DEPTH_AMPLITUDE_MAX,amplitude);
        if (virtual_depth_amplitude != amplitude) {
            virtual_depth_amplitude = amplitude;
            if (lighting.position().z < virtual_depth_amplitude) {
                lighting.position().z = virtual_depth_amplitude;
            }
        }
    }
    
    public OrthographicCamera camera() { return camera; }
    
    public Channel currentChannel() {
        return currentChannel;
    }
    
    public CanvasBackground background() {
        return graphics.background();
    }
    
    public CanvasGrid grid() {
        return graphics.grid();
    }
    
    public Brush brush() {
        return brush;
    }
    
    public PreviewLighting lighting() {
        return lighting;
    }
    
    public PreviewDisplay previewDisplay() {
        return previewDisplay;
    }
    
    public void setPreviewDisplay(PreviewDisplay previewDisplay) {
        this.previewDisplay = previewDisplay;
    }
    
    public void togglePreviewLighting() {
        preview_lighting = !preview_lighting;
    }
    
    public void togglePreviewShadow() {
        preview_shadow = !preview_shadow;
    }
    
    public void togglePreviewPalette() {
        preview_palette = !preview_palette;
    }
    
    public Palette currentPalette() {
        return currentPalette;
    }
    
    public void setPalette(Palette palette) {
        this.currentPalette = palette;
    }
    
    public void setPaletteDefault() {
        this.currentPalette = defaultPalette;
    }
    
    public float detailVolumeRatio() {
        return detail_to_volume_ratio;
    }
    
    public float depthAmplitude() {
        return virtual_depth_amplitude;
    }
    
    public boolean isCurrentlyEditing() {
        return currently_editing;
    }
    
    public boolean isCurrentlyDragging() {
        return currently_dragging;
    }
    
    public boolean projectIsOpen() {
        return activeProject != null;
    }
    
    public boolean previewLighting() {
        return preview_lighting;
    }
    
    public boolean previewShadow() {
        return preview_shadow;
    }
    
    public boolean previewPalette() {
        return preview_palette;
    }
    
    public int screenWidth() {
        return screen_width;
    }
    
    public int screenHeight() {
        return screen_height;
    }
    
    private void uploadProjectUniformBlock() {
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(PROJECT_UNIFORMS_SIZE_FLOAT);
            activeProject.getUniforms(buffer);
            uniformBufferProject.bind();
            uniformBufferProject.bufferSubData(buffer.flip(),0);
        }
    }
    
    private void uploadEditorUniformBlock() {
        try (MemoryStack stack = MemoryStack.stackPush()){
            FloatBuffer buffer = stack.mallocFloat(EDITOR_UNIFORMS_SIZE_FLOAT);
            BufferObject.put(camera.combined(),buffer);
            BufferObject.put(camera.combinedINV(),buffer);
            BufferObject.put(camera.position,buffer);
            buffer.put(camera.zoom);
            BufferObject.put(mouseCurrent,buffer);
            buffer.put(virtual_depth_amplitude);
            buffer.put(detail_to_volume_ratio);
            BufferObject.putPadding(2,buffer);
            buffer.put(shader_time_accumulator);
            buffer.put(previewOptions()).flip();
            uniformBufferEditor.bind();
            uniformBufferEditor.bufferSubData(buffer,0);
        }
    }
    
    private int previewOptions() {
        int preview_options = previewDisplay.value;
        preview_options |= ( preview_lighting ? 8  : 0);
        preview_options |= ( preview_shadow   ? 16 : 0);
        preview_options |= ( preview_palette  ? 32 : 0);
        return preview_options;
    }
    
    public void dispose() {
        closeAllProjects();
        Shaders.dispose();
        Disposable.dispose(graphics,brush);
    }
}
