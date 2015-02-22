/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package game2D;

/**
 *
 * @author ryanremer
 */
public class Collision 
{
    int feetY;
    int feetX;
    int offset;
    int yo;
    int tileWidth;
    int tileHeight;
    static int groundHeight = 352;
    TileMap tmap;
    public Collision(TileMap tmap)
    {
        this.tmap=tmap;
        tileWidth = tmap.getTileWidth();
        tileHeight = tmap.getTileHeight();
    }
    public void colUpdate(int offset,int yo)
    {
      this.offset=offset; 
      this.yo=yo;
    }
    public char feetCollision(Sprite s,long elapsed)
    {     
        // location of feet different for inverse
        
        if(s.getDirectionLeft() == true)
        {
           feetX = Math.floorDiv(((int)s.getX()-offset-11),tileWidth)+1; 
        }
        else
        {
         feetX = Math.floorDiv(((int)s.getX()-offset-20),tileWidth)+1;   
        }
         
        feetY = Math.floorDiv(((int)(s.getY()+s.getHeight()+3-yo)),tileHeight);
        Tile tileFeet = tmap.getTile(feetX, feetY);
       // System.out.println(s.getHeight());    
        if(tileFeet != null && tileFeet.getCharacter() == '.')
        {
            s.setInAir(true);
        }
        else if(tileFeet != null && (tileFeet.getCharacter() == 'p' || tileFeet.getCharacter() == 'm' ||tileFeet.getCharacter() == 'b' &&  (s.getY()-s.getHeight()-yo) <= groundHeight))
        {          
            int newGroundHeight = tileFeet.getYC();

            if ((s.getY() + s.getHeight()-yo)>newGroundHeight)
                {            
        	   // Put the player back on the map    
                    System.out.println("!!!");
                    s.setY(newGroundHeight - s.getHeight()+yo);
                    s.setVelocityY(-0.005f);                  
                    s.setInAir(false);       	
                 }   
            return 'p';
          
        }  
        else if(tileFeet.getCharacter() == 'f')
        {
            Sound sound = new Sound("sounds/hit.wav");
            //sound.start();
            
            s.setVelocityY(-0.4f);
            return 'f';
        }
        else if(tileFeet.getCharacter() == 'c')
        {
          Sound coinCollect = new Sound("sounds/smb_coin.wav");
         // coinCollect.start();
          tmap.setTileChar('x', feetX, feetY);
          return 'c';
        }
        return '.';
        
         // System.out.println(s.getVelocityY());
    }
    public char sideCollision(Sprite s,long elapsed)
    {
        
        int noseX;
        int noseY = Math.floorDiv((int)(s.getY()+16-yo),tileHeight);
        
        if(noseY<feetY)
        {
         // This checks for any collision with characters nose
        if(s.getDirectionRight())
        {
          noseX = Math.floorDiv((int)(s.getX()-7-offset),tileWidth)+1;
        }
        else
        {
          noseX = Math.floorDiv((int)(s.getX()-offset-s.getWidth()-4),tileWidth)+1;
        }
        
        
        Tile t = tmap.getTile(noseX, noseY);
        
        
        if(s.getX()>10 && t.getCharacter() == 'p')
        {
           if(s.getDirectionRight())
           {
                System.out.println("col right");
                s.setCol(true);
                s.setDirectionRight(false);
                s.setVelocityX(-0.03f);  
                s.update(elapsed);
                Sound sound = new Sound("sounds/bump.wav");
    		//sound.start();
              //  s.setX(t.getXC()-s.getWidth()+offset);
                
              return'p';
           }
           else
           {
                s.setVelocityX(0.03f);   
                s.update(elapsed);
                Sound sound = new Sound("sounds/bump.wav");
    		//sound.start();
                s.setCol(true);
                s.setDirectionLeft(false);
              //  s.setX(t.getXC()+offset+tileWidth);
              
               
           }
        }      
        else
        {
          s.setCol(false); 
          
        }
        if(t.getCharacter() == 'c')
        {
          Sound coinCollect = new Sound("sounds/smb_coin.wav");
        //  coinCollect.start();
          tmap.setTileChar('x', noseX, noseY);
          return 'c';
          
        }
       
        }
       
        return '.';
        
    }
    public Tile headCollision(Sprite s,long elapsed)
    {
        
        int noseX;
        int noseY = Math.floorDiv((int)(s.getY()-yo),tileHeight);
        
        if(noseY<feetY)
        {
         // This checks for any collision with characters nose
        if(s.getDirectionRight() == true)
        {
          noseX = Math.floorDiv((int)(s.getX()-12-offset),tileWidth)+1;
        }
        else
        {
          noseX = Math.floorDiv((int)(s.getX()-offset-15),tileWidth)+1;
        }
          //noseX = Math.floorDiv((int)(s.getX()-offset-12),tileWidth)+1;
        
        
        
        Tile t = tmap.getTile(noseX, noseY);
     //   System.out.println(noseX+" "+noseY);
        
        if(noseY<=0||t.getCharacter() == 'p'||t.getCharacter() == 'b')
        {               
                Sound sound = new Sound("sounds/bump.wav");
    	//	sound.start();
                s.setDirectionUp(false);
                s.setY(t.getYC()+32+yo);
                s.setVelocityY(0.004f);   
                s.update(elapsed);
                return t;
           
        }    
        else if(t.getCharacter()=='m'&& s.getType().equals("mario"))
        {           
            s.setTileX(noseX);
            s.setTileY(noseY);
            System.out.println("boom");
            s.setDirectionUp(false);
            s.setY(t.getYC()+33+yo);
            s.setVelocityY(0.04f);     
            s.update(elapsed);   
            //tmap.setTileChar('b',noseX,noseY);
            return t;
            
        }
        if(t.getCharacter() == 'c' && s.getType().equals("mario"))
        {
            System.out.println("head");
          Sound coinCollect = new Sound("sounds/smb_coin.wav");
         // coinCollect.start();
          tmap.setTileChar('x', noseX, noseY);
          return t;
        }
       
       
        }
        return null;
        
    }
     public void backHeadCollision(Sprite s,long elapsed)
    {
        int noseY = Math.floorDiv((int)(s.getY()+16-yo),tileHeight);
        if(s.getVelocityX()>0 && s.getDirectionRight() == false)
        {
            
            int noseX = Math.floorDiv((int)(s.getX()-offset-10),tileWidth)+1;
            Tile t = tmap.getTile(noseX, noseY);
            if(t != null && t.getCharacter() == 'p')
            {

                    System.out.println("Bhead");
                    s.setCol(true);
                    s.setDirectionLeft(false);
                   // s.setX(t.getXC()+offset+tileWidth);
                    s.setVelocityX(-0.03f);   
                    s.update(elapsed);

            }      
            else
            {
               s.setCol(false); 
            }
        }
        
    }
     public void coinCollision(Sprite s,long elapsed)
    {
       int coinX = Math.floorDiv(((int)s.getX()),tileWidth); 
       int coinY = Math.floorDiv((int) s.getY()+33, tileHeight);
       Tile t = tmap.getTile(coinX, coinY); 
        if(t.getCharacter() == 'b' && s.getVelocityY()>0)
        {                  
                s.setY(t.getYC()-32);
                s.setVelocityY(-s.getVelocityY() * (0.03f * elapsed));
                s.update(elapsed);
        } 
    }
}
