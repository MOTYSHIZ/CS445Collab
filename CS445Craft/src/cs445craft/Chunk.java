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
                        } else if (s == y) {
                            //creates top most layer
                            
                            if(r.nextFloat() > .75) {
                                Blocks[(int)x][(int)y][(int)z] = 
                                        new Block(Block.BlockType.BlockType_Grass);
                            } else if(r.nextFloat() > .5) {
                                Blocks[(int) x][(int)y][(int) z] = 
                                        new Block(Block.BlockType.BlockType_Glowstone);
                            } else if(r.nextFloat() > .25) {
                                Blocks[(int)x][(int)y][(int)z] =
                                        new Block(Block.BlockType.BlockType_Sand);
                            }else {
                                Blocks[(int)x][(int)y][(int)z] = 
                                        new Block(Block.BlockType.BlockType_Water);
                            }
                            VertexTextureData.put(createTexCube((float) 0, (float) 0,
                                Blocks[(int)(x)][(int) (y)][(int) (z)]));
                            
                        }else { //creates innermost layers of THE EARTH! 
                            if(r.nextFloat() > .5) {
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
        
        //Add correct layers here
        
        
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
    
    public static float[] createCube(float x, float y, float z) {
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
                x + offset* 3, y + offset*10,
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
                x + offset*0 , y + offset*9,
                x + offset*1, y + offset*9,
                x + offset*1, y + offset*10,
                x + offset*0, y + offset*10,
                // Bottom
                x + offset*0 , y + offset*9,
                x + offset*1, y + offset*9,
                x + offset*1, y + offset*10,
                x + offset*0, y + offset*10,
                // FRONT QUAD
                x + offset*0 , y + offset*9,
                x + offset*1, y + offset*9,
                x + offset*1, y + offset*10,
                x + offset*0, y + offset*10,
                // BACK QUAD
                x + offset*0 , y + offset*9,
                x + offset*1, y + offset*9,
                x + offset*1, y + offset*10,
                x + offset*0, y + offset*10,
                // LEFT QUAD
                x + offset*0 , y + offset*9,
                x + offset*1, y + offset*9,
                x + offset*1, y + offset*10,
                x + offset*0, y + offset*10,
                // RIGHT QUAD
                x + offset*0 , y + offset*9,
                x + offset*1, y + offset*9,
                x + offset*1, y + offset*10,
                x + offset*0, y + offset*10 };
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
                x + offset*0, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*0, y + offset*2,
                // Bottom
                x + offset*0, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*0, y + offset*2,
                // FRONT QUAD
                x + offset*0, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*0, y + offset*2,
                // BACK QUAD
                x + offset*0, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*0, y + offset*2,
                // LEFT QUAD
                x + offset*0, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*0, y + offset*2,
                // RIGHT QUAD
                x + offset*0, y + offset*1,
                x + offset*1, y + offset*1,
                x + offset*1, y + offset*2,
                x + offset*0, y + offset*2};
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
}