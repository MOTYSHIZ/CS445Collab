/***************************************************************
* file: FPCameraController.java
* author: Justin Ordonez & Kacey Yahl
* class: CS 445 – Computer Graphics
*
* assignment: Final Project Checkpoint 2
* date last modified: 11/19/2015
*
* purpose: Creates a controllable camera for viewing the scene.
* Also contains the gameLoop() method which is essentially the method that the program
* spends most of its time in.
*
****************************************************************/ 

package cs445craft;

import java.nio.FloatBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;


public class FPCameraController {
    //3d vector to store the camera's position in
    private Vector3f position = null;
    private Vector3f lPosition= null;
    //the rotation around the Y axis of the camera
    private float yaw = 0.0f;
    //the rotation around the X axis of the camera
    private float pitch = 0.0f;
    private Vector3f me;
    private Chunk chunk;
    private boolean day = true;
    private boolean cycle = false;
    private float[][] frustum = new float[6][4];
    
    private float xOffset;
    private float zOffset;

    /** method: FPCameraController
     * purpose: Constructor for this class. Parameters are the starting 
     * coordinates for the camera.
    **/   
    public FPCameraController(float x, float y, float z){
        //instantiate position Vector3f to the x y z params.
        position = new Vector3f(x, y, z);
        lPosition= new Vector3f(x,y,z);
        lPosition.x = 30f;
        lPosition.y = 30f;
        lPosition.z = 30f;
        
        Random r = new Random(); //helps generate chunks of random height
        chunk = new Chunk(0, r.nextInt(50), 0);
        //chunk = new Chunk(0, 0, 0);
    }
    
    /** method: yaw
     * purpose: Increments the camera's current yaw rotation
    **/   
    public void yaw(float amount){
        //increment the yaw by the amount param
        yaw += amount;
    }
    
    
    /** method: pitch
    * purpose: Increments the camera's current pitch.
    **/   
    public void pitch(float amount){
        //increment the pitch by the amount param
        pitch -= amount;
    }
    
    /** method: walkForward
    * purpose: Moves the camera forward relative to its current rotation (yaw)
    **/ 
    public void walkForward(float distance){
        xOffset= distance * (float)Math.sin(Math.toRadians(yaw));
        zOffset= distance * (float)Math.cos(Math.toRadians(yaw));
        position.x-= xOffset;
        position.z+= zOffset;
        
        //Light movement
        FloatBuffer lightPosition= BufferUtils.createFloatBuffer(4);
        lightPosition.put(lPosition.x+=xOffset).put(lPosition.y).put(lPosition.z-=zOffset).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    /** method: walkBackwards
    * purpose: Moves the camera backward relative to its current rotation (yaw)
    **/ 
    public void walkBackwards(float distance){
        xOffset= distance * (float)Math.sin(Math.toRadians(yaw));
        zOffset= distance * (float)Math.cos(Math.toRadians(yaw));
        position.x+= xOffset;
        position.z-= zOffset;
        
        //Light movement
        FloatBuffer lightPosition= BufferUtils.createFloatBuffer(4);
        lightPosition.put(lPosition.x-=xOffset).put(lPosition.y).put(lPosition.z+=zOffset).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    /** method: strafeLeft
    * purpose: Strafes the camera left relative to its current rotation (yaw)
    **/ 
    public void strafeLeft(float distance){
        xOffset= distance * (float)Math.sin(Math.toRadians(yaw-90));
        zOffset= distance * (float)Math.cos(Math.toRadians(yaw-90));
        position.x-= xOffset;
        position.z+= zOffset;
        
        //Light movement
        FloatBuffer lightPosition= BufferUtils.createFloatBuffer(4);
        lightPosition.put(lPosition.x-=xOffset).put(lPosition.y).put(lPosition.z+=zOffset).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    /** method: strafeRight
    * purpose: Strafes the camera right relative to its current rotation (yaw)
    **/ 
    public void strafeRight(float distance){
        xOffset= distance * (float)Math.sin(Math.toRadians(yaw+90));
        zOffset= distance * (float)Math.cos(Math.toRadians(yaw+90));
        position.x-= xOffset;
        position.z+= zOffset;
        
        //Light movement
        FloatBuffer lightPosition= BufferUtils.createFloatBuffer(4);
        lightPosition.put(lPosition.x-=xOffset).put(lPosition.y).put(lPosition.z+=zOffset).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    /** method: moveUp
    * purpose: Moves the camera up
    **/
    public void moveUp(float distance){
        position.y-= distance;
        
    }
    
    /** method: moveDown
    * purpose: Moves the camera down
    **/
    public void moveDown(float distance){
        position.y+= distance;
    }
    
    /** method: lookThrough
    * purpose: translates and rotate the matrix so that it looks through the 
    * camera this does basically what gluLookAt() does
    **/
    public void lookThrough(){
        //rotate the pitch around the X axis
        glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        //rotate the yaw around the Y axis
        glRotatef(yaw, 0.0f, 1.0f, 0.0f);
        //translate to the position vector's location
        glTranslatef(position.x, position.y, position.z);
        
        //for Lighting
        FloatBuffer lightPosition= BufferUtils.createFloatBuffer(4);
        lightPosition.put(lPosition.x).put(lPosition.y).put(lPosition.z).put(1.0f).flip();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition);
    }
    
    /** method: gameLoop
    * purpose: Listens for user-input to determine where the camera moves.
    * Also makes the call to the Chunk's render class.
    * Pressing ESC closes the program.
    **/
    public void gameLoop(){
        final FPCameraController camera = new FPCameraController(0, 0, 0);
        float dx = 0.0f;
        float dy= 0.0f;
        float dt= 0.0f; //length of frame
        float lastTime= 0.0f; // when the last frame was
        long time = 0;
        float mouseSensitivity= 0.09f;
        float movementSpeed= .35f;
        //hide the mouse
        Mouse.setGrabbed(true);
        
        // keep looping till the display window is closed the ESC key is down
        while (!Display.isCloseRequested() && 
                !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
            time = Sys.getTime();
            lastTime= time;
            //distance in mouse movement //from the last getDX() call.
            dx = Mouse.getDX();
            //distance in mouse movement //from the last getDY() call.
            dy= Mouse.getDY();
            //controllcamera yaw from x movement fromtthe mouse
            camera.yaw(dx * mouseSensitivity);
            //controllcamera pitch from y movement fromtthe mouse
            camera.pitch(dy* mouseSensitivity);
            
            //when passing in the distance to move
            //we times the movementSpeed with dt this is a time scale
            //so if its a slow frame u move more then a fast frame
            //so on a slow computer you move just as fast as on a fast computer
            if (Keyboard.isKeyDown(Keyboard.KEY_W) ||
                    Keyboard.isKeyDown(Keyboard.KEY_UP)){    //move forward
                camera.walkForward(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S) ||
                    Keyboard.isKeyDown(Keyboard.KEY_DOWN)){    //move backwards
                camera.walkBackwards(movementSpeed);
            }
            
            if (Keyboard.isKeyDown(Keyboard.KEY_A) ||
                    Keyboard.isKeyDown(Keyboard.KEY_LEFT)){    //strafe left 
                camera.strafeLeft(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D) ||
                    Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {   //strafe right
                camera.strafeRight(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)){    //move up 
                camera.moveUp(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {     //move down  
                camera.moveDown(movementSpeed);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_F)) {     //change lighting  
                if(day){
                    camera.lPosition.x -= 10000f;
                    day = false;
                }else{
                    camera.lPosition.x  = 30f;
                    camera.lPosition.z = 30f;
                    day = true;
                }
            }
            if(Keyboard.isKeyDown(Keyboard.KEY_L)) { //day night cycle
                cycle = !cycle;
            }
            
            if(cycle) {
                
                if(camera.lPosition.x < 200) {
                    camera.lPosition.x += 2f;
                    
                } else {
                    camera.lPosition.x = -200f;
                }
            } else {
                
            }
            
            //set the modelviewmatrix back to the identity
            glLoadIdentity();
            //look through the camera before you draw anything
            camera.lookThrough();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST); //Allows depth perception
            
            //you would draw your scene here.
            chunk.render();
            
            //draw the buffer to the screen
            Display.update();
            Display.sync(60);
        }
        
        Display.destroy();
    }
    
    /** method: getFrustrum
    * purpose: Computes the six planes of the frustrum from the projection and 
    * modelview matrices so that they can be used to determine if a point lies
    * in the frustrum for frustrum culling.
    * 
    * Modified from code found at:
    * http://www.crownandcutlass.com/features/technicaldetails/frustum.html
    **/  
    private void getFrustrum(){
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
        float[] combine = new float[16];
        float normalizer;
        
        glGetFloat(GL_PROJECTION_MATRIX,projection);
        glGetFloat(GL_MODELVIEW_MATRIX,modelview);
        
        float[] proj = projection.array();
        float[] modl = modelview.array();
        
        int j=0;
        for(int i = 0 ; i < 4 ; i++){
          combine[i]=(modl[0]*proj[j]) + (modl[1]*proj[j+4]) + 
                  (modl[2]*proj[j+8]) + (modl[3]*proj[j+12]);
          j++;
        } j=0;
        for(int i = 4 ; i < 8 ; i++){
          combine[i]=(modl[4]*proj[j]) + (modl[5]*proj[j+4]) + 
                  (modl[6]*proj[j+8]) + (modl[7]*proj[j+12]);
          j++;
        } j=0;
        for(int i = 8 ; i < 12 ; i++){
          combine[i]=(modl[8]*proj[j]) + (modl[9]*proj[j+4]) + 
                  (modl[10]*proj[j+8]) + (modl[11]*proj[j+12]);
          j++;
        } j=0;
        for(int i = 12 ; i < 16 ; i++){
          combine[i]=(modl[12]*proj[j]) + (modl[13]*proj[j+4]) + 
                  (modl[14]*proj[j+8]) + (modl[15]*proj[j+12]);
          j++;
        }
        
        //Extracting numbers for RIGHT plane.
        frustum[0][0] = combine[3] - combine[0];
        frustum[0][1] = combine[7] - combine[4];
        frustum[0][2] = combine[11] - combine[8];
        frustum[0][3] = combine[15] - combine[12];
        
        //Normalizing result
        normalizer = (float)Math.sqrt(frustum[0][0] * frustum[0][0] + frustum[0][1] * 
                frustum[0][1] + frustum[0][2] * frustum[0][2] );
        frustum[0][0] /= normalizer;
        frustum[0][1] /= normalizer;
        frustum[0][2] /= normalizer;
        frustum[0][3] /= normalizer;
        
        // Extracting numbers for LEFT plane 
        frustum[1][0] = combine[3] + combine[0];
        frustum[1][1] = combine[7] + combine[4];
        frustum[1][2] = combine[11] + combine[8];
        frustum[1][3] = combine[15] + combine[12];

        /* Normalizing result */
        normalizer = (float)Math.sqrt( frustum[1][0] * frustum[1][0] + frustum[1][1] * 
                frustum[1][1] + frustum[1][2] * frustum[1][2] );
        frustum[1][0] /= normalizer;
        frustum[1][1] /= normalizer;
        frustum[1][2] /= normalizer;
        frustum[1][3] /= normalizer;
        
        // Extracting numbers for BOTTOM plane 
        frustum[2][0] = combine[3] + combine[1];
        frustum[2][1] = combine[7] + combine[5];
        frustum[2][2] = combine[11] + combine[9];
        frustum[2][3] = combine[15] + combine[13];
        
        /* Normalizing result */
        normalizer = (float)Math.sqrt( frustum[2][0] * frustum[2][0] + frustum[2][1] * 
                frustum[2][1] + frustum[2][2] * frustum[2][2] );
        frustum[2][0] /= normalizer;
        frustum[2][1] /= normalizer;
        frustum[2][2] /= normalizer;
        frustum[2][3] /= normalizer;
        
        // Extracting numbers for TOP plane 
        frustum[3][0] = combine[3] - combine[1];
        frustum[3][1] = combine[7] - combine[5];
        frustum[3][2] = combine[11] - combine[ 9];
        frustum[3][3] = combine[15] - combine[13];
        
        /* Normalizing result */
        normalizer = (float)Math.sqrt( frustum[3][0] * frustum[3][0] + frustum[3][1] * 
                frustum[3][1] + frustum[3][2] * frustum[3][2] );
        frustum[3][0] /= normalizer;
        frustum[3][1] /= normalizer;
        frustum[3][2] /= normalizer;
        frustum[3][3] /= normalizer;
        
        // Extracting numbers for FAR plane
        frustum[4][0] = combine[3] - combine[2];
        frustum[4][1] = combine[7] - combine[6];
        frustum[4][2] = combine[11] - combine[10];
        frustum[4][3] = combine[15] - combine[14];
        
        /* Normalizing result */
        normalizer = (float)Math.sqrt( frustum[4][0] * frustum[4][0] + frustum[4][1] * 
                frustum[4][1] + frustum[4][2] * frustum[4][2] );
        frustum[4][0] /= normalizer;
        frustum[4][1] /= normalizer;
        frustum[4][2] /= normalizer;
        frustum[4][3] /= normalizer;
        
        // Extracting numbers for NEAR plane
        frustum[5][0] = combine[3] + combine[2];
        frustum[5][1] = combine[7] + combine[6];
        frustum[5][2] = combine[11] + combine[10];
        frustum[5][3] = combine[15] + combine[14];
        
        /* Normalizing result */
        normalizer = (float)Math.sqrt( frustum[5][0] * frustum[5][0] + frustum[5][1] * 
                frustum[5][1] + frustum[5][2] * frustum[5][2] );
        frustum[5][0] /= normalizer;
        frustum[5][1] /= normalizer;
        frustum[5][2] /= normalizer;
        frustum[5][3] /= normalizer;
    }
    
    /** method: pointInFrustrum
    * purpose: Checks if a point is in the frustrum by seeing if it is a
    * positive distance away from each frustrum plane.
    * Returns true if point is within frustrum.
    * 
    * Modified from code found at:
    * http://www.crownandcutlass.com/features/technicaldetails/frustum.html
    **/  
    private boolean pointInFrustum( float x, float y, float z ){
       for(int p = 0; p < 6; p++ )
          if( frustum[p][0] * x + frustum[p][1] * y + frustum[p][2] * z + 
                  frustum[p][3] <= 0 )
             return false;
       return true;
    }
    
    //Only used for Checkpoint 1
    private void render() {
        try{
            glBegin(GL_QUADS);
                //top
                glColor4f(0.0f, 0.0f, 1.0f, 0.5f);
                glVertex3f(1.0f, 1.0f, -1.0f);
                glVertex3f(-1.0f, 1.0f,-1.0f);
                glVertex3f(-1.0f, 1.0f, 1.0f);
                glVertex3f( 1.0f, 1.0f, 1.0f);
                //bottom
                glColor4f(1.0f, 0.0f, 0.0f, 0.5f);
                glVertex3f( 1.0f,-1.0f, 1.0f);
                glVertex3f(-1.0f,-1.0f, 1.0f);
                glVertex3f(-1.0f,-1.0f,-1.0f);
                glVertex3f( 1.0f,-1.0f,-1.0f);
                //front
                glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
                glVertex3f( 1.0f, 1.0f, 1.0f);
                glVertex3f(-1.0f, 1.0f, 1.0f);
                glVertex3f(-1.0f,-1.0f, 1.0f);
                glVertex3f( 1.0f,-1.0f, 1.0f);
                //back
                glColor4f(1.0f,0.0f,1.0f, 0.5f);
                glVertex3f( 1.0f,-1.0f,-1.0f);
                glVertex3f(-1.0f,-1.0f,-1.0f);
                glVertex3f(-1.0f, 1.0f,-1.0f);
                glVertex3f( 1.0f, 1.0f,-1.0f); 
                //left
                glColor4f(1.0f, 1.0f, 0.0f, 0.5f);
                glVertex3f(-1.0f, 1.0f,1.0f);
                glVertex3f(-1.0f, 1.0f,-1.0f);
                glVertex3f(-1.0f,-1.0f,-1.0f);
                glVertex3f(-1.0f,-1.0f, 1.0f);
                //right
                glColor4f(0.0f, 1.0f, 1.0f, 0.5f);
                glVertex3f( 1.0f, 1.0f,-1.0f);
                glVertex3f( 1.0f, 1.0f, 1.0f);
                glVertex3f( 1.0f,-1.0f, 1.0f);
                glVertex3f( 1.0f,-1.0f,-1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
                //Top
                glColor3f(0.0f,0.0f,0.0f);
                glVertex3f( 1.0f, 1.0f,-1.0f);
                glVertex3f(-1.0f, 1.0f,-1.0f);
                glVertex3f(-1.0f, 1.0f, 1.0f);
                glVertex3f( 1.0f, 1.0f, 1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
                //Bottom
                glVertex3f( 1.0f,-1.0f, 1.0f);
                glVertex3f(-1.0f,-1.0f, 1.0f);
                glVertex3f(-1.0f,-1.0f,-1.0f);
                glVertex3f( 1.0f,-1.0f,-1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
                //Front
                glVertex3f( 1.0f, 1.0f, 1.0f);
                glVertex3f(-1.0f, 1.0f, 1.0f);
                glVertex3f(-1.0f,-1.0f, 1.0f);
                glVertex3f( 1.0f,-1.0f, 1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
                //Back
                glVertex3f( 1.0f,-1.0f,-1.0f);
                glVertex3f(-1.0f,-1.0f,-1.0f);
                glVertex3f(-1.0f, 1.0f,-1.0f);
                glVertex3f( 1.0f, 1.0f,-1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
                //Left
                glVertex3f(-1.0f, 1.0f, 1.0f);
                glVertex3f(-1.0f, 1.0f,-1.0f);
                glVertex3f(-1.0f,-1.0f,-1.0f);
                glVertex3f(-1.0f,-1.0f, 1.0f);
            glEnd();
            glBegin(GL_LINE_LOOP);
                //Right
                glVertex3f( 1.0f, 1.0f,-1.0f);
                glVertex3f( 1.0f, 1.0f, 1.0f);
                glVertex3f( 1.0f,-1.0f, 1.0f);
                glVertex3f( 1.0f,-1.0f,-1.0f);
            glEnd();           
        }catch(Exception e){
        }
    }
}