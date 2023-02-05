package io.github.heathensoft.canvas;

import io.github.heathensoft.canvas.brush.Brush;
import io.github.heathensoft.canvas.f.Project;
import io.github.heathensoft.canvas.f.UndoRedoManager;
import io.github.heathensoft.canvas.f.io.PngImporter;
import io.github.heathensoft.canvas.light.PointLight;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.common.storage.generic.Container;
import io.github.heathensoft.jlib.common.utils.Area;
import io.github.heathensoft.jlib.common.utils.Coordinate;
import io.github.heathensoft.jlib.common.utils.DiscreteLine;
import io.github.heathensoft.jlib.common.utils.IDPool;
import io.github.heathensoft.jlib.lwjgl.graphics.Palette;
import io.github.heathensoft.jlib.lwjgl.utils.MathLib;
import io.github.heathensoft.jlib.lwjgl.utils.OrthographicCamera;
import io.github.heathensoft.jlib.lwjgl.window.Keyboard;
import io.github.heathensoft.jlib.lwjgl.window.Mouse;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;
import org.joml.*;
import org.joml.Math;
import org.tinylog.Logger;


import java.util.*;

import static io.github.heathensoft.canvas.f.ENUM.*;
import static io.github.heathensoft.canvas.f.ENUM.BrushShape.ROUND;
import static io.github.heathensoft.canvas.f.ENUM.BrushShape.SQUARE;
import static io.github.heathensoft.canvas.f.ENUM.BrushTool.*;
import static org.lwjgl.glfw.GLFW.*;


/**
 * @author Frederik Dahl
 * 31/01/2023
 */


public class Editor implements Disposable {
    
    public static final float DETAIL_TO_VOLUME_RATIO_DEFAULT = 0.5f;
    public static final float DEPTH_AMPLITUDE_DEFAULT = 8.0f;
    public static final float DEPTH_AMPLITUDE_MIN = 1.0f;
    public static final float DEPTH_AMPLITUDE_MAX = 16.0f;
    public static final float CAMERA_ZOOM_MAX =  4.0f; // 0 = 100%
    public static final float CAMERA_ZOOM_MIN = -4.0f;
    
    private final EditorRenderer renderer;
    private final IDPool project_id_pool;
    private final Container<Project> projects_by_id;
    private final List<Project> project_list;
    
    private final Area brush_drag_area;
    private final DiscreteLine lineDraw_coordinates;
    private final Set<Coordinate> freeHand_coordinates;
    private final Vector2f cursor_position;
    private final Vector2f cursor_position_start;
    private final Coordinate cursor_coordinate;
    private final Coordinate cursor_coordinate_start;
    private final Area editable_area_bounds;
    
    public final OrthographicCamera editor_camera;
    public final Matrix4f screen_combined_inv;
    public final Matrix4f screen_combined;
    public final Matrix4f right_side_unproject_mat;
    public final Matrix4f left_side_unproject_mat;
    public final int screen_width;
    public final int screen_height;
    
    public Palette current_palette;
    public Project active_project;
    public Channel current_channel;
    public PreviewDisplay preview_display;
    public PointLight light;
    public Brush brush;
    public boolean calculate_lighting;
    public boolean calculate_shadow;
    public boolean use_palette;
    
    private float detail_to_volume_ratio;
    private float current_zoom_amount;
    private float virtual_depth_amplitude;
    private boolean currently_editing;
    
    
    
    
    public Editor(Resolution resolution) throws Exception {
    
        this.renderer = new EditorRenderer();
        this.project_id_pool = new IDPool();
        this.projects_by_id = new Container<>(16);
        this.project_list = new ArrayList<>(16);
        
        this.lineDraw_coordinates = new DiscreteLine(0,0,0,0);
        this.freeHand_coordinates = new HashSet<>(137); // arbitrary prime
        this.brush_drag_area = new Area();
        this.editable_area_bounds = new Area();
        this.cursor_position = new Vector2f();
        this.cursor_position_start = new Vector2f();
        this.cursor_coordinate = new Coordinate();
        this.cursor_coordinate_start = new Coordinate();
    
        this.screen_width = resolution.width();
        this.screen_height = resolution.height();
        this.screen_combined = new Matrix4f();
        this.screen_combined.ortho(0,screen_width,0,screen_height,0.01f,1);
        this.screen_combined.mul(MathLib.mat4().identity().lookAt(
                0,0,1, 0,0,-1, 0,1,0));
        this.screen_combined_inv = new Matrix4f(screen_combined).invert();
        this.left_side_unproject_mat = new Matrix4f(
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.0f, 0.0f, 1.0f).invert();
        this.right_side_unproject_mat = new Matrix4f(
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.0f, 0.0f, 1.0f).invert();
        this.editor_camera = new OrthographicCamera();
        this.editor_camera.viewport.set(screen_width / 2f, screen_height);
        this.editor_camera.refresh();
    
        
        this.brush = new Brush();
        this.light = new PointLight();
        this.current_channel = Channel.DEFAULT;
        this.preview_display = PreviewDisplay.DEFAULT;
        this.virtual_depth_amplitude = DEPTH_AMPLITUDE_DEFAULT;
        this.detail_to_volume_ratio = DETAIL_TO_VOLUME_RATIO_DEFAULT;
        this.current_zoom_amount = 0.0f;
        
    }
    
    public void render(float frame_time) {
    
    }
    
    public void process(Mouse mouse, Keyboard keys) {
        
        unProjectMouse(mouse.ndc(), cursor_position);
        cursor_coordinate.set(
                (int) cursor_position.x,
                (int) cursor_position.y);
        process_input(mouse, keys);
        
        if (projectIsOpen()) {
            editing(mouse);
        } else currently_editing = false;
        
    }
    
    private void process_input(Mouse mouse, Keyboard keys) {
        
        final boolean PROJECT_OPEN = projectIsOpen();
        final boolean CTRL = keys.pressed(GLFW_KEY_LEFT_CONTROL) || keys.pressed(GLFW_KEY_RIGHT_CONTROL);
        final boolean SHIFT = keys.pressed(GLFW_KEY_LEFT_SHIFT) || keys.pressed(GLFW_KEY_RIGHT_SHIFT);
        final boolean ALT = keys.pressed(GLFW_KEY_LEFT_ALT) || keys.pressed(GLFW_KEY_RIGHT_ALT);
        final boolean SCROLLED = mouse.scrolled();
    
        if (CTRL && ALT) {
        
            /* */
        
        } else if (CTRL && SHIFT) {
        
            // REDO
            if (keys.just_pressed(GLFW_KEY_Z) && PROJECT_OPEN) {
                active_project.undoRedoManager().redo();
            }
        
        
        } else if(CTRL) {
        
            if (SCROLLED) {
            
                // BRUSH SIZE
                int increment = 0;
                float amount = mouse.get_scroll();
                if (amount < 0) increment++;
                else if (amount > 0) increment--;
                brush.incrementBrushSize(increment);
            
            } else {
            
                // UNDO REDO
                if (keys.just_pressed(GLFW_KEY_Z) && PROJECT_OPEN) {
                    active_project.undoRedoManager().undo();
                } else if (keys.just_pressed(GLFW_KEY_Y) && PROJECT_OPEN) {
                    active_project.undoRedoManager().redo();
                }
                
                // TOGGLE PALETTE
                else if (keys.just_pressed(GLFW_KEY_P)) {
                    use_palette = !use_palette;
                }
            
                // TOGGLE LIGHTING
                else if (keys.just_pressed(GLFW_KEY_L)) {
                    calculate_lighting = !calculate_lighting;
                }
            
                // TOGGLE SHADOWS
                else if (keys.just_pressed(GLFW_KEY_S)) {
                    calculate_shadow = !calculate_shadow;
                }
            
                // TOGGLE FUNCTION
                else if (keys.just_pressed(GLFW_KEY_LEFT)) {
                    brush.setFunction(brush.function().prev());
                } else if (keys.just_pressed(GLFW_KEY_RIGHT)) {
                    brush.setFunction(brush.function().next());
                }
            
                // ADJUST DETAIL TO VOLUME RATIO
                else if (keys.just_pressed(GLFW_KEY_UP)) {
                    float increment = 1 / 8f;
                    setDetailToVolumeRatio(detail_to_volume_ratio + increment);
                } else if (keys.just_pressed(GLFW_KEY_RIGHT)) {
                    float increment = 1 / 8f;
                    setDetailToVolumeRatio(detail_to_volume_ratio - increment);
                }
            
                // BRUSH COLOR INCREMENT LEAP
                else if (keys.just_pressed(GLFW_KEY_KP_ADD)) {
                    brush.incrementColor(16);
                } else if (keys.just_pressed(GLFW_KEY_KP_SUBTRACT)) {
                    brush.decrementColor(16);
                }
            
            }
        
        } else if (ALT) {
    
            // SAVE / OVERWRITE
            if (projectIsOpen()) {
                try {
                    if(keys.just_pressed(GLFW_KEY_A)) {
                        active_project.saveAll(true);
                    } else if (keys.just_pressed(GLFW_KEY_N)) {
                        active_project.saveNormals(true);
                    } else if (keys.just_pressed(GLFW_KEY_P)) {
                        active_project.savePreview(true);
                    } else if (keys.just_pressed(GLFW_KEY_S)) {
                        active_project.save(current_channel,true);
                    }
                } catch (Exception e) {
                    Logger.error(e,"unable to save project");
                }
                
            }
            
        
        } else if (SHIFT) { /* */
        
        } else {
        
            if (SCROLLED) {
            
                float amount = mouse.get_scroll();
            
                // LIGHT Z-POSITION
                if (mouse.button_pressed(Mouse.RIGHT)) {
                    light.position().z -= (amount * virtual_depth_amplitude);  // both for scrolled and ! scrolled
                    if (light.position().z < virtual_depth_amplitude) {
                        light.position().z = virtual_depth_amplitude;
                    }
                }
            
                else { // CAMERA ZOOM
                    float zoom_raw = current_zoom_amount -= amount;
                    zoom_raw = Math.clamp(CAMERA_ZOOM_MIN, CAMERA_ZOOM_MAX, zoom_raw);
                    if (current_zoom_amount != zoom_raw) {
                        current_zoom_amount = zoom_raw;
                        int pow = (int) current_zoom_amount;
                        editor_camera.zoom = (float) java.lang.Math.pow(2,pow);
                    }
                }
            
            
            } else {
            
                { // OPEN PROJECT SLOT
                    int i = 1;
                    for (Project project : project_list) {
                        if (i == 10) break;
                        if (keys.just_pressed(GLFW_KEY_0 + i)) {
                            openProject(project.projectID());
                        } i++;
                    }
                }
            
                // TOOL SELECTION
                if (keys.just_pressed(GLFW_KEY_I)) {        // SAMPLER
                    brush.setTool(SAMPLER);
                } else if (keys.just_pressed(GLFW_KEY_L)) { // LINE DRAW
                    brush.setTool(LINE_DRAW);
                } else if (keys.just_pressed(GLFW_KEY_B)) { // FREE HAND
                    brush.setTool(FREE_HAND);
                } else if (keys.just_pressed(GLFW_KEY_U)) { // DRAG AREA
                    if (brush.tool() == DRAG_AREA) {
                        if (brush.shape() == ROUND) {       // SHAPE TOGGLE
                            brush.setShape(SQUARE);
                        } else brush.setShape(ROUND);
                    } else brush.setTool(DRAG_AREA);
                }
                
                // BRUSH SHAPE
                else if (keys.just_pressed(GLFW_KEY_R)) {
                    brush.setShape(ROUND);
                } else if (keys.just_pressed(GLFW_KEY_S)) {
                    brush.setShape(SQUARE);
                }
            
                // TOGGLE CHANNEL
                else if (keys.just_pressed(GLFW_KEY_LEFT)) {
                    current_channel = current_channel.prev();
                } else if (keys.just_pressed(GLFW_KEY_RIGHT)) {
                    current_channel = current_channel.next();
                }
            
                // TOGGLE PREVIEW DISPLAY
                else if (keys.just_pressed(GLFW_KEY_UP)) {
                    preview_display = preview_display.prev();
                } else if (keys.just_pressed(GLFW_KEY_DOWN)) {
                    preview_display = preview_display.next();
                }
            
                // BRUSH COLOR INCREMENT
                else if (keys.just_pressed(GLFW_KEY_KP_ADD)) {
                    brush.incrementColor(1);
                } else if (keys.just_pressed(GLFW_KEY_KP_SUBTRACT)) {
                    brush.decrementColor(1);
                }
            
                // CAMERA TRANSLATION
            
                if (mouse.is_dragging(Mouse.WHEEL)) {
                    Vector2f drag_vec = mouse.delta_vector();
                    editor_camera.position.x -= drag_vec.x * screen_width * editor_camera.zoom;
                    editor_camera.position.y -= drag_vec.y * screen_height * editor_camera.zoom;
                }
            
                // POINT LIGHT POSITION
                else if (mouse.is_dragging(Mouse.RIGHT)) {
                    float light_position_z = light.position().z;
                    light.position().set(
                            cursor_position.x,
                            cursor_position.y,
                            light_position_z
                    );
                }
            }
        }
    }
    
    private void editing(Mouse mouse) {
        if (currently_editing) {
            if (mouse.just_released(Mouse.LEFT)) {
                // FINISHED EDITING
                currently_editing = false;
                if (brush.tool() != SAMPLER) {
                    UndoRedoManager urManager = active_project.undoRedoManager();
                    Vector4f bounds = active_project.bounds();
                    Area projectArea = new Area(
                            (int) bounds.x, (int) bounds.y,
                            (int) bounds.z, (int) bounds.w);
                    switch (brush.tool()) {
                        
                        case FREE_HAND -> {
                            
                            if (!freeHand_coordinates.isEmpty()) {
                                int min_x = Integer.MAX_VALUE;
                                int min_y = Integer.MAX_VALUE;
                                int max_x = Integer.MIN_VALUE;
                                int max_y = Integer.MIN_VALUE;
                                for(Coordinate c : freeHand_coordinates) {
                                    min_x = java.lang.Math.min(min_x,c.x);
                                    min_y = java.lang.Math.min(min_y,c.y);
                                    max_x = java.lang.Math.max(max_x,c.x);
                                    max_y = java.lang.Math.max(max_y,c.y);
                                }int trans = brush.textureSize() / 2;
                                Area editArea = new Area(min_x,min_y,max_x,max_y);
                                editArea.translate(trans,trans);
                                editArea.expand(brush.brushSize() / 2);
                                if (projectArea.intersects(editArea)) {
                                    urManager.newEdit(editArea,current_channel,brush);
                                    // renderer draw to back
                                }
                            }
                        }
                        case LINE_DRAW -> {
                            
                            Area editArea = new Area(lineDraw_coordinates.p0(),lineDraw_coordinates.p1());
                            editArea.expand(brush.brushSize() / 2);
                            if (projectArea.intersects(editArea)) {
                                urManager.newEdit(editArea,current_channel,brush);
                                // renderer draw to back
                            }
                        }
                        case DRAG_AREA -> {
                            
                            Area editArea = new Area(brush_drag_area);
                            if (projectArea.intersects(editArea)) {
                                urManager.newEdit(editArea,current_channel,brush);
                                // renderer draw to back
                            }
                        }
                    }
                }
                
            } else { // EDITING
                
                switch (brush.tool()) {
                    
                    case SAMPLER -> brush_drag_area.set(cursor_coordinate);
                    case FREE_HAND -> {
                        
                        if (editable_area_bounds.contains(cursor_coordinate)) {
                            int x0 = (int)(cursor_position.x - brush.textureSize() / 2f);
                            int y0 = (int)(cursor_position.y - brush.textureSize() / 2f);
                            Coordinate coordinate = new Coordinate(x0,y0);
                            freeHand_coordinates.add(coordinate);
                        }
                    }
                    case LINE_DRAW -> {
                        
                        Coordinate end = editable_area_bounds.closestPoint(
                                cursor_coordinate,new Coordinate());
                        lineDraw_coordinates.set(cursor_coordinate_start,end);
                    }
                    case DRAG_AREA -> brush_drag_area.set(
                            cursor_coordinate_start,
                            cursor_coordinate);
                    default -> throw new IllegalStateException("Unexpected value: " + brush.tool());
                }
            }
            
        } else if (mouse.just_started_drag(Mouse.LEFT)) {
            // START EDITING
            freeHand_coordinates.clear();
            cursor_position_start.set(cursor_position);
            cursor_coordinate_start.set(
                    (int) cursor_position.x,
                    (int) cursor_position.y);
            currently_editing = true;
        }
    }
    
    
    
    public int newProject(PngImporter.Textures textures) {
        int id = project_id_pool.obtainID();
        Project project = null;
        try { project = new Project(textures,id);
            projects_by_id.set(project.projectID(),project);
            project_list.add(project);
            project_list.sort(Comparator.naturalOrder());
            openProject(id);
        } catch (Exception e) {
            project_id_pool.returnID(id);
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
    
    public void closeProject(int project_id) {
        Project project = getProject(project_id);
        if (project != null) {
            if (active_project == project) {
                setActiveProject(null);
            } projects_by_id.set(project_id,null);
            project_id_pool.returnID(project_id);
            project_list.remove(project);
            if (!project_list.isEmpty()) {
                project_list.sort(Comparator.naturalOrder());
            } project.dispose();
        }
    }
    
    public void closeActiveProject() {
        if (active_project != null) {
            closeProject(active_project.projectID());
        }
    }
    
    private void setActiveProject(Project project) {
        if (project != null) {
            Vector4f bounds = project.bounds();
            editable_area_bounds.set(
                    (int) bounds.x, (int) bounds.y,
                    (int) bounds.z, (int) bounds.w);
            editable_area_bounds.expand(brush.textureSize() / 2);
        } active_project = project;
        
    }
    
    public void closeAllProjects() {
        projects_by_id.collect(project -> closeProject(project.projectID()));
    }
    
    public Project getProject(int project_id) {
        if (projects_by_id.capacity() - 1 >= project_id) {
           return projects_by_id.get(project_id);
        } return null;
    }
    
    public List<Project> getProjects() {
        return project_list;
    }
    
    public boolean projectIsOpen() {
        return active_project != null;
    }
    
    
    public void unProjectMouse(Vector2f mouseNDC, Vector2f dest) {
        Vector3f v3 = MathLib.vec3(mouseNDC.x,mouseNDC.y,0);
        if (v3.x > 0) v3.mulProject(right_side_unproject_mat);
        else v3.mulProject(left_side_unproject_mat);
        dest.set(v3.x,v3.y);
        editor_camera.unProject(dest);
    }
    
    public float detailToVolumeRatio() {
        return detail_to_volume_ratio;
    }
    
    public void setDetailToVolumeRatio(float ratio) {
        detail_to_volume_ratio = Math.clamp(0.0f,1.0f,ratio);
    }
    
    public float depthAmplitude() {
        return virtual_depth_amplitude;
    }
    
    public void setDepthAmplitude(float amplitude) {
        amplitude = Math.abs(amplitude);
        amplitude = Math.clamp(DEPTH_AMPLITUDE_MIN,DEPTH_AMPLITUDE_MAX,amplitude);
        if (virtual_depth_amplitude != amplitude) {
            virtual_depth_amplitude = amplitude;
            if (light.position().z < virtual_depth_amplitude) {
                light.position().z = virtual_depth_amplitude;
            }
        }
    }
    
    public void setPalette(String name) {
    
    }
    
    public void dispose() {
        
        closeAllProjects();
        Disposable.dispose(
                brush
        );
    }
}
