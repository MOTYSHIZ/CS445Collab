/***************************************************************
* file: CS445Craft.java
* author: Justin Ordonez & Kacey Yahl
* class: CS 445 – Computer Graphics
*
* assignment: Final Project
* date last modified: 11/29/2015
*
* purpose: Main class of the program, calls the other classes from its
* start() method. Create terrain with simplex noise, add in textures,
* and make sure everything is in a chunk. 
* 
* Textures from Riverwood texture pack made by Steelfeathers
*
****************************************************************/ 

package cs445craft;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.GLU;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;


public class CS445Craft {
    private FPCameraController fp;
    private DisplayMode displayMode;
    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;


    /** method: start
     * purpose: This method calls all the methods to set up
     * our rendering environment.
    **/   
    public void start() {
        try {
            createWindow();
            initGL();
            fp = new FPCameraController(0f,0f,0f);
            fp.gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /** method: createWindow
     * purpose: This method creates a display window with a set title at 640 x 480 resolution.
    **/    
    private void createWindow() throws Exception{
        Display.setFullscreen(false);
        DisplayMode d[] = Display.getAvailableDisplayModes();
        
        for (int i= 0; i < d.length; i++) {
            if (d[i].getWidth() == 640 && d[i].getHeight() == 480 
                    && d[i].getBitsPerPixel() == 32) {
                displayMode = d[i];
                break;
            }
        }
        
        Display.setDisplayMode(displayMode); 
        Display.setTitle("Aw, Yiss. THREE DEEZ!");
        Display.create();
    }
    
    /** method: initGL
     * purpose: This method initiates openGL for the window and 
     * sets various properties like the background color and 
     * how the coordinate system is set up.
    **/    
    private void initGL() {
        glClearColor(0.0f, 0.6f, 1.0f, 0.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(100.0f, (float)displayMode.getWidth()/(float) 
                displayMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        
        //added for Chunk render
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnable(GL_DEPTH_TEST);
        
        //These are enabled for texture mapping.
        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        
        //Enables transparency for txtures
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        //Lighting
        initLightArrays();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition); //sets our light’s position
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);//sets our specular light
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);//sets our diffuse light
        glLight(GL_LIGHT0, GL_AMBIENT, whiteLight);//sets our ambient light
        
        glEnable(GL_LIGHTING);//enables our lighting
        glEnable(GL_LIGHT0);//enables light0
        
    }
    
    private void initLightArrays() {
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(0.0f).put(0.0f).put(0.0f).put(1.0f).flip();
        
        whiteLight= BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
    }
    
    /** method: main
     * purpose: This method is where the flow of the program starts and ends.
    **/   
    public static void main(String[] args) {
        CS445Craft basic = new CS445Craft();
        basic.start();
    }
}
