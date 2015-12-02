/***************************************************************
* file: Chunk.java
* author: Justin Ordonez & Kacey Yahl
* class: CS 445 – Computer Graphics
*
* assignment: Final Project Checkpoint 2
* date last modified: 11/19/2015
*
* purpose: Creates a chunk to be rendered. Has its own render method, which
* will be the primary rendering method for the program. This class uses the
* Block class to create and texture the blocks that build up the scene.
*
****************************************************************/ 

package cs445craft;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

//Texture Mapping imports
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class Chunk {
    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    private Block[][][] Blocks;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int VBOTextureHandle;
    private Texture texture;
    private int StartX, StartY, StartZ;
    private Random r;
    private SimplexNoise sn;
    private float[][] frustum = new float[6][4];
    
    public void render(){
        glPushMatrix();
            glBindBuffer(GL_ARRAY_BUFFER,VBOVertexHandle);
            glVertexPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER,VBOColorHandle);
            glColorPointer(3,GL_FLOAT, 0, 0L);
            
            //Texture mapping
            glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
            glBindTexture(GL_TEXTURE_2D, 1);
            glTexCoordPointer(2,GL_FLOAT,0,0L);
            
            glDrawArrays(GL_QUADS, 0,CHUNK_SIZE *CHUNK_SIZE*CHUNK_SIZE * 24);
        glPopMatrix();
        
        //insert rendering stuff here
    }

    public void rebuildMesh(float startX, float startY, float startZ) { 
        VBOColorHandle= glGenBuffers();
        VBOVertexHandle= glGenBuffers();
        VBOTextureHandle= glGenBuffers();
        
        FloatBuffer VertexPositionData= BufferUtils.createFloatBuffer((CHUNK_SIZE
                * CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexColorData= BufferUtils.createFloatBuffer((CHUNK_SIZE* 
                CHUNK_SIZE * CHUNK_SIZE) * 6 * 12);
        FloatBuffer VertexTextureData =BufferUtils.createFloatBuffer((CHUNK_SIZE*
                CHUNK_SIZE *CHUNK_SIZE)* 6 * 12);
        
        //Apply noise here
        int seed = r.nextInt(5000);
        sn = new SimplexNoise(r.nextInt(CHUNK_SIZE), .15, seed);
        float y = 0;
        for (float x = 0; x < CHUNK_SIZE; x += 1) {
            for (float z = 0; z < CHUNK_SIZE; z += 1) {
                
                int noiseValue = Math.abs((int)(100 * sn.getNoise((int)x, (int)y, (int)z))); //-100 to 100
                if(noiseValue == 0) {
                    noiseValue = 1;
                }

                float height = startY + noiseValue * CUBE_LENGTH;
                height = (3*height/25);
                double s = Math.ceil((double)height);
                
                for(y = 0; y < CHUNK_SIZE; y++){
                    if(s >= y) {
                        VertexPositionData.put(createCube((float) 
                            (startX+ x *CUBE_LENGTH),
                            (float)(y*CUBE_LENGTH+(int)(CHUNK_SIZE*.8)),
                            (float) (startZ+ z * CUBE_LENGTH)));
                        VertexColorData.put(createCubeVertexCol(
                            getCubeColor(Blocks[(int) x][(int) y][(int) z])));
                        
                        if(y == 0) { //creates bedrock layer
                            Blocks[(int)x][(int)y][(int)z] = new Block(Block.BlockType.BlockType_Bedrock);
                            VertexTextureData.put(createTexCube((float) 0, (float) 0,
                                Blocks[(int)(x)][(int) (y)][(int) (z)]));
                        } else if (s == y) { //creates top most layer
                            if(r.nextFloat() > .5) {
                                Blocks[(int)x][(int)y][(int)z] = 
                                        new Block(Block.BlockType.BlockType_Grass);
                                VertexTextureData.put(createTexCube((float) 0, (float) 0,
                                Blocks[(int)(x)][(int) (y)][(int) (z)]));
                            } else if(r.nextFloat() > .3) {
                                Blocks[(int) x][(int)y][(int) z] = 
                                        new Block(Block.BlockType.BlockType_Sand);
                                VertexTextureData.put(createTexCube((float) 0, (float) 0,
                                Blocks[(int)(x)][(int) (y)][(int) (z)]));
                            
                            }else {
                                Blocks[(int)x][(int)y][(int)z] = 
                                        new Block(Block.BlockType.BlockType_Water);
                                VertexTextureData.put(createTexCube((float) 0, (float) 0,
                                Blocks[(int)(x)][(int) (y)][(int) (z)]));
                            }
                            
                        }else { //creates innermost layers of THE EARTH! 
                            if(r.nextFloat() > .55) {
                                Blocks[(int)x][(int) y][(int)z] = 
                                        new Block(Block.BlockType.BlockType_Stone);
                            } else {
                                Blocks[(int)x][(int)y][(int)z] =
                                        new Block(Block.BlockType.BlockType_Dirt);
                            }
                            VertexTextureData.put(createTexCube((float) 0, (float) 0,
                                Blocks[(int)(x)][(int) (y)][(int) (z)]));
                        } 
                        
                    }
                    
                }
            }
        }
      
        VertexColorData.flip();
        VertexPositionData.flip();
        VertexTextureData.flip();
        
        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexPositionData,GL_STATIC_DRAW);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexColorData,GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData,
            GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    private float[] createCubeVertexCol(float[] CubeColorArray) {
        float[] cubeColors= new float[CubeColorArray.length* 4 * 6];

        for (int i= 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i%CubeColorArray.length];
        }
        return cubeColors;
    }
    
    public float[] createCube(float x, float y, float z) {
        int offset = CUBE_LENGTH / 2;

            return new float[] {
            // TOP QUAD
            x + offset, y + offset, z,
            x -offset, y + offset, z,
            x -offset, y + offset, z -CUBE_LENGTH,
            x + offset, y + offset, z -CUBE_LENGTH,
            // BOTTOM QUAD
            x + offset, y -offset, z -CUBE_LENGTH,
            x -offset, y -offset, z -CUBE_LENGTH,
            x -offset, y -offset, z,
            x + offset, y -offset, z,
            // FRONT QUAD
            x + offset, y + offset, z -CUBE_LENGTH,
            x -offset, y + offset, z -CUBE_LENGTH,
            x -offset, y -offset, z -CUBE_LENGTH,
            x + offset, y -offset, z -CUBE_LENGTH,
            // BACK QUAD
            x + offset, y -offset, z,
            x -offset, y -offset, z,
            x -offset, y + offset, z,
            x + offset, y + offset, z,
            // LEFT QUAD
            x -offset, y + offset, z -CUBE_LENGTH,
            x -offset, y + offset, z,
            x -offset, y -offset, z,
            x -offset, y -offset, z -CUBE_LENGTH,
            // RIGHT QUAD
            x + offset, y + offset, z,
            x + offset, y + offset, z -CUBE_LENGTH,
            x + offset, y -offset, z -CUBE_LENGTH,
            x + offset, y -offset, z };
    }
    
    private float[] getCubeColor(Block block) {
        return new float[] { 1, 1, 1 };
    }
    
    public Chunk(int startX, int startY, int startZ) {
        try{
            texture = TextureLoader.getTexture("PNG", 
            ResourceLoader.getResourceAsStream("terrain.png"));
        }catch(Exception e){
            System.out.print("ER-ROAR!");
        }
        
        r= new Random();
        Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        
        VBOColorHandle= glGenBuffers();
        VBOVertexHandle= glGenBuffers();
        VBOTextureHandle= glGenBuffers();
        
        StartX= startX;
        StartY= startY;
        StartZ= startZ;
        
        rebuildMesh(startX, startY, startZ);
    }
    
    public static float[] createTexCube(float x, float y, Block block) {
        float offset = (1024f/16)/1024f;
        
        switch (block.GetID()) {
            case 0 : //Grass
                return new float[] {
                // Top
                x + offset* 4, y + offset*2,
                x + offset*3, y + offset*2,
                x + offset*3, y + offset*1,
                x + offset*4, y + offset*1,
                // Bottom 
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
            case 1 : //Sand
                return new float[] {
                // Top
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                // Bottom 
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                // FRONT QUAD
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                // BACK QUAD
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                // LEFT QUAD
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2,
                // RIGHT QUAD
                x + offset*2, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*2,
                x + offset*2, y + offset*2};
            case 2 : //Water
                return new float[] {
                // Top
                x + offset*15 , y + offset*13,
                x + offset*16, y + offset*13,
                x + offset*16, y + offset*14,
                x + offset*15, y + offset*14,
                // Bottom
                x + offset*15 , y + offset*13,
                x + offset*16, y + offset*13,
                x + offset*16, y + offset*14,
                x + offset*15, y + offset*14,
                // FRONT QUAD
                x + offset*15 , y + offset*13,
                x + offset*16, y + offset*13,
                x + offset*16, y + offset*14,
                x + offset*15, y + offset*14,
                // BACK QUAD
                x + offset*15 , y + offset*13,
                x + offset*16, y + offset*13,
                x + offset*16, y + offset*14,
                x + offset*15, y + offset*14,
                // LEFT QUAD
                x + offset*15, y + offset*13,
                x + offset*16, y + offset*13,
                x + offset*16, y + offset*14,
                x + offset*15, y + offset*14,
                // RIGHT QUAD
                x + offset*15, y + offset*13,
                x + offset*16, y + offset*13,
                x + offset*16, y + offset*14,
                x + offset*15, y + offset*14 };
            case 3 : //Dirt
                return new float[] {
                // Top
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // Bottom
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // BACK QUAD
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // RIGHT QUAD
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0};
            case 4 : //Stone
                return new float[] {
                // Top
                x + offset*2, y + offset*0,
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // Bottom
                x + offset*2, y + offset*0,
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // FRONT QUAD
                x + offset*2, y + offset*0,
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // BACK QUAD
                x + offset*2, y + offset*0,
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // LEFT QUAD
                x + offset*2, y + offset*0,
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                // RIGHT QUAD
                x + offset*2, y + offset*0,
                x + offset*1, y + offset*0,
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1};
            case 5 : //Bedrock
                return new float[] {
                // Top
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                // Bottom
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                // FRONT QUAD
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                // BACK QUAD
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                // LEFT QUAD
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2,
                // RIGHT QUAD
                x + offset*1, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*2,
                x + offset*1, y + offset*2};
            case 6 : //Glowstone 
                return new float[] {
                // Top
                x + offset*0, y + offset*3,
                x + offset*1, y + offset*3,
                x + offset*1, y + offset*4,
                x + offset*0, y + offset*4,
                // Bottom
                x + offset*0, y + offset*3,
                x + offset*1, y + offset*3,
                x + offset*1, y + offset*4,
                x + offset*0, y + offset*4,
                // FRONT QUAD
                x + offset*0, y + offset*3,
                x + offset*1, y + offset*3,
                x + offset*1, y + offset*4,
                x + offset*0, y + offset*4,
                // BACK QUAD
                x + offset*0, y + offset*3,
                x + offset*1, y + offset*3,
                x + offset*1, y + offset*4,
                x + offset*0, y + offset*4,
                // LEFT QUAD
                x + offset*0, y + offset*3,
                x + offset*1, y + offset*3,
                x + offset*1, y + offset*4,
                x + offset*0, y + offset*4,
                // RIGHT QUAD
                x + offset*0, y + offset*3,
                x + offset*1, y + offset*3,
                x + offset*1, y + offset*4,
                x + offset*0, y + offset*4};
            case 7: //cactus
                return new float[] {
                //Top
                x + offset*6, y + offset*5,
                x + offset*5, y + offset*5,
                x + offset*5, y + offset*4,
                x + offset*6, y + offset*4,
                //Bottom
                x + offset*8, y + offset*5,
                x + offset*7, y + offset*5,
                x + offset*7, y + offset*4,
                x + offset*8, y + offset*4,
                //Front
                x + offset*6, y + offset*4,
                x + offset*7, y + offset*4,
                x + offset*7, y + offset*5,
                x + offset*6, y + offset*5,
                //Back
                x + offset*6, y + offset*4,
                x + offset*7, y + offset*4,
                x + offset*7, y + offset*5,
                x + offset*6, y + offset*5,
                //Left
                x + offset*6, y + offset*4,
                x + offset*7, y + offset*4,
                x + offset*7, y + offset*5,
                x + offset*6, y + offset*5,
                //Right
                x + offset*6, y + offset*4,
                x + offset*7, y + offset*4,
                x + offset*7, y + offset*5,
                x + offset*6, y + offset*5};
            default:
                return new float[] {
                // Top
                x + offset*3, y + offset*10,
                x + offset*2, y + offset*10,
                x + offset*2, y + offset*9,
                x + offset*3, y + offset*9,
                // Bottom
                x + offset*3, y + offset*1,
                x + offset*2, y + offset*1,
                x + offset*2, y + offset*0,
                x + offset*3, y + offset*0,
                // FRONT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // BACK QUAD
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                // LEFT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1,
                // RIGHT QUAD
                x + offset*3, y + offset*0,
                x + offset*4, y + offset*0,
                x + offset*4, y + offset*1,
                x + offset*3, y + offset*1};
        }
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
    
    /** method: cubeInFrustrum
    * purpose: Checks if a cube is in the frustrum by calling pointInFrustrum()
    * for every vertex in the cube.
    * Returns true if cube is within frustrum.
    **/  
    private boolean cubeInFrustrum(float x, float y, float z) {
        boolean inFrust = false;
        int offset = CUBE_LENGTH / 2;
            //Top,Bottom,Front and Back Quad vertices ommitted because they are essentially
            //The same verices as the Left and Right quads combined.
        
            // LEFT QUAD
            if(pointInFrustum(x -offset, y + offset, z -CUBE_LENGTH))inFrust = true;
            if(pointInFrustum(x -offset, y + offset, z))inFrust = true;
            if(pointInFrustum(x -offset, y -offset, z))inFrust = true;
            if(pointInFrustum(x -offset, y -offset, z -CUBE_LENGTH))inFrust = true;
            // RIGHT QUAD
            if(pointInFrustum(x + offset, y + offset, z))inFrust = true;
            if(pointInFrustum(x + offset, y + offset, z -CUBE_LENGTH))inFrust = true;
            if(pointInFrustum(x + offset, y -offset, z -CUBE_LENGTH))inFrust = true;
            if(pointInFrustum(x + offset, y -offset, z))inFrust = true;
            
            return inFrust;
    }
}

