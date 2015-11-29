/***************************************************************
* file: Block.java
* author: Justin Ordonez & Kacey Yahl
* class: CS 445 – Computer Graphics
*
* assignment: Final Project Checkpoint 2
* date last modified: 11/19/2015
*
* purpose: The block class that will essentially contain the information that each 
* block in the game world holds. For each block, holds information on the block's 
* type, if it is active, and its coordinates.
*
****************************************************************/ 

package cs445craft;

public class Block {
    private boolean IsActive;
    private BlockType Type;
    private float x,y,z;
    
    /*
    Enumerating Block Types
    */
    public enum BlockType{
        BlockType_Grass(0), 
        BlockType_Sand(1), 
        BlockType_Water(2), 
        BlockType_Dirt(3), 
        BlockType_Stone(4),  
        BlockType_Bedrock(5), 
        BlockType_Glowstone(6),
        BlockType_Cactus(7);
        
        private int BlockID;
        
        BlockType(int i) {
            BlockID=i;
        }
        
        public int GetID(){
            return BlockID; 
        }
        public void SetID(int i){
            BlockID= i;
        }
    }
    
    /** method: Block
     * purpose: Constructor for this class. Parameter is the type of the Block
    **/   
    public Block(BlockType type){
        Type= type;
    }

    /** method: setCoords
     * purpose: Used to set the coordinates of the block
    **/ 
    public void setCoords(float x, float y, float z){
        this.x= x;
        this.y= y;
        this.z= z;
    }
    
    /** method: isActive
     * purpose: Gets boolean value for whether or not the block is active.
    **/ 
    public boolean isActive() {
        return IsActive;
    }
    
    /** method: setActive
     * purpose: Depending on the boolean value input, sets block as active or 
     * inactive.
    **/ 
    public void SetActive(boolean active){
        IsActive=active;
    }
    
    /** method: getID
     * purpose: Gets the block's type. 
     **/ 
    public int GetID(){
        return Type.GetID();
    }
}