/***************************************************************
* file: CS445Craft.java
* author: Justin Ordonez & Kacey Yahl
* class: CS 445 – Computer Graphics
*
* assignment: Final Program Checkpoint 1
* date last modified: 11/4/2015
*
* purpose: This program displays a cube which has 6 differently
* colored sides. You can rotate and move the cube by using the mouse and
* WASD + left shift and space.
*****************************************************************/

package CS445Craft;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.GLU;

public class CS445Craft {
    private FPCameraController fp = new FPCameraController(0f,0f,0f);
    private DisplayMode displayMode;

    /** method: start
     * purpose: This method calls all the methods to set up
     * our rendering environment.
    **/   
    public void start() {
        try {
            createWindow();
            initGL();
            fp.gameLoop();
            //render();
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
        Display.setTitle("WOWEE! The ULTIMATE THING!");
        Display.create();
    }
    
    /** method: initGL
     * purpose: This method initiates openGL for the window and 
     * sets various properties like the background color and 
     * how the coordinate system is set up.
    **/    
    private void initGL() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(100.0f, (float)displayMode.getWidth()/(float) 
                displayMode.getHeight(), 0.1f, 300.0f);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }
    
    /** method: main
     * purpose: This method is where the flow of the program starts and ends.
    **/   
    public static void main(String[] args) {
        CS445Craft basic = new CS445Craft();
        basic.start();
    }
}
