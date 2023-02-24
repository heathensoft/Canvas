package io.github.heathensoft.canvas.split;

import io.github.heathensoft.canvas.neo.SplitScreen;
import io.github.heathensoft.jlib.common.Disposable;
import io.github.heathensoft.jlib.lwjgl.graphics.*;
import io.github.heathensoft.jlib.lwjgl.utils.Resources;
import io.github.heathensoft.jlib.lwjgl.window.Resolution;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

/**
 * @author Frederik Dahl
 * 22/02/2023
 */


public class Renderer implements Disposable {
    
    SpriteBatch batch;
    public SplitScreen splitScreen;
    ShaderProgram splitScreenShader;
    Texture contourTexture;
    
    
    public Renderer(Resolution resolution) throws Exception {
        splitScreen = new SplitScreen(resolution);
        batch = new SpriteBatch(8);
        Resources io = new Resources();
        splitScreenShader = new ShaderProgram(
                io.asString("res/glsl/editor/test/split.vert"),
                io.asString("res/glsl/editor/test/split.frag")
        );//splitScreenShader.createUniform("u_combined");
        splitScreenShader.createUniform("u_sampler_2D");
        Image img = io.image("res/contourImage.png");
        contourTexture = Texture.image2D(img,false);
        img.dispose();
        contourTexture.bindToActiveSlot();
        contourTexture.wrapST(GL_CLAMP_TO_EDGE);
        contourTexture.nearest();
    }
    
    public void render() {
        
        splitScreen.refresh();
        glDisable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_SCISSOR_TEST);
        Framebuffer.bindDefault();
        
        splitScreenShader.use();
        splitScreenShader.setUniform1i("u_sampler_2D",0);
        contourTexture.bindToSlot(0);
        
        if (splitScreen.leftScreenWidth() > 0) {
            splitScreen.viewportLeft();
            splitScreen.scissorLeft();
            Framebuffer.setClearColor(0,1,1,1);
            Framebuffer.clear();
            batch.begin();
            float dividingLine = splitScreen.dividingLine();
            float u = 0.5f - (dividingLine / 2.0f);
            float u2 =  0.5f + (dividingLine / 2.0f);
            batch.draw(u,0,u2,1,-1,-1,2,2,Color.WHITE.toFloatBits(),0);
            batch.end();
        }
        if (splitScreen.rightScreenWidth() > 0) {
            splitScreen.viewportRight();
            splitScreen.scissorRight();
            Framebuffer.setClearColor(0,1,0,1);
            Framebuffer.clear();
            batch.begin();
            float dividingLine = splitScreen.dividingLine();
            float u = 0.5f - ((1 - dividingLine) / 2f);
            float u2 =  0.5f + ((1 - dividingLine) / 2f);
            batch.draw(u,0,u2,1,-1,-1,2,2,Color.RED.toFloatBits(),0);
            //batch.draw(-12,-12,24,24, Color.RED.toFloatBits(),0);
            batch.end();
        }
        
        splitScreen.scissorRestore();
        glDisable(GL_SCISSOR_TEST);
    }
    
    public void dispose() {
        Disposable.dispose(splitScreenShader,batch,contourTexture);
    }
}
