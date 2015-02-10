import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.awt.Rectangle;
import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. By default GameCore
// will handle the 'Escape' key to quit the game but you should
// override this with your own event handler.

/**
 * @author David Cairns
 *
 */
@SuppressWarnings("serial")

public class Game extends GameCore 
{
	// Useful game constants
	static int screenWidth = 512;
	static int screenHeight = 384;
        
        static int groundHeight = 352;

    float 	lift = 0.005f;
    float	gravity = 0.0006f;
    
    // Game state flags
    boolean flapUp = false;
    boolean flapRight = false;
    boolean flapLeft = false;
    boolean inAir = false;
    boolean directionRight = true;
    boolean noseColl = false;
    boolean feetColl = false;
    
    // Game resources
    Animation landing;
    
    Sprite	player = null;
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();

    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
    Tile nearestTile;
    int tileWidth ;
    int tileHeight;
    int offset;
    long total;         			// The score will be the total time elapsed since a crash
    int feetY;
    int feetX;

    /**
	 * The obligatory main method that creates
     * an instance of our class and starts it running
     * 
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args)
    {

        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false,screenWidth,screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers
     */
    public void init()
    {         
        Sprite s;	// Temporary reference to a sprite

        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map.txt");
        tileWidth = tmap.getTileWidth();
        tileHeight = tmap.getTileHeight();
        // Create a set of background sprites that we can 
        // rearrange to give the illusion of motion
        
        landing = new Animation();
        landing.loadAnimationFromSheet("images/landbird.png", 5, 1, 60);
        
        // Initialise the player with an animation
        player = new Sprite(landing);
        
        // Load a single cloud animation
        Animation ca = new Animation();
        ca.addFrame(loadImage("images/cloud.png"), 1000);
        
        // Create 3 clouds at random positions off the screen
        // to the right
        for (int c=0; c<3; c++)
        {
        	s = new Sprite(ca);
        	s.setX(screenWidth + (int)(Math.random()*200.0f));
        	s.setY(30 + (int)(Math.random()*150.0f));
        	s.setVelocityX(-0.02f);
        	s.show();
        	clouds.add(s);
        }

        initialiseGame();
      		
        
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it to restart
     * the game.
     */
    public void initialiseGame()
    {
    	total = 0;
    	      
        player.setX(32);
        player.setY(groundHeight-player.getHeight()-30);
        player.setVelocityX(0);
        player.setVelocityY(0);
        player.show();
    }
    
    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {    	
    	// Be careful about the order in which you draw objects - you
    	// should draw the background first, then work your way 'forward'

    	// First work out how much we need to shift the view 
    	// in order to see where the player is.
        int xo = (int)(64-player.getX());
        int yo = 0;
       

        // If relative, adjust the offset so that
        // it is relative to the player

        // ...?
        
        g.setColor(new Color(100,100,250,150));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Apply offsets to sprites then draw them
        for (Sprite s: clouds)
        {
        	s.setOffsets(xo,yo);
        	s.draw(g);
        }

        // Apply offsets to player and draw         
        player.setOffsets(xo, yo);
        
        player.drawTransformed(g);
                
        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo);    
        // Show score and status information
        String msg = String.format("Score: %d", total/100);
        g.setColor(Color.red);
        g.drawString(msg, getWidth() - 80, 50);
       
        
        /*g.drawRect((int)player.getX()+20,(int)player.getY()+12,5,5);
        if(directionRight == false)
        {
            g.drawRect((int)player.getX()+20-player.getWidth()+5,(int)player.getY()+12,5,5);
        }
       g.drawRect((int)player.getX()+6,(int)player.getY()+player.getHeight()-3,7,5);*/
         
    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	
        // Make adjustments to the speed of the sprite due to gravity
        player.setVelocityY(player.getVelocityY()+(gravity*elapsed));
    	offset = (int)(64-player.getX());
          
        
        if(flapRight && inAir == false) // Do not allow movement mid jump
        {           
          player.playAnimation();
          player.setVelocityX(0.04f);
          player.setAnimationSpeed(1.0f);
          flapRight = true;
        }         
       	if(flapLeft && inAir == false)
        {
            
           player.playAnimation();
           player.setVelocityX(-0.04f);
           player.setAnimationSpeed(1.0f);
           flapRight = false;
        } 
        if(flapLeft == false && flapRight == false && inAir == false)
        {
            player.setVelocityX(0.0f);
            player.pauseAnimationAtFrame(2);
        }
       	if (flapUp) 
       	{      		
       		player.setVelocityY(-0.15f);                                 
       	}
        
          
           
          
           
       	for (Sprite s: clouds)
       		s.update(elapsed);
       	
        // Now update the sprites animation and position
        player.update(elapsed);
       // update nearest tiles
         
            
         
        // Then check for any collisions that may have occurred
        handleTileMapCollisions(player,elapsed);
         	
    }
    
    
    /**
     * Checks and handles collisions with the tile map for the
     * given sprite 's'. Initial functionality is limited...
     * 
     * @param s			The Sprite to check collisions for
     * @param elapsed	How time has gone by
     */
    public void handleTileMapCollisions(Sprite s, long elapsed)
    {
    	// This method should check actual tile map collisions. For
    	// now it just checks if the player has gone off the bottom
    	// of the tile map.
       
       
         
        if (s.getY() + player.getHeight()> groundHeight+1)
        {
            
        	// Put the player back on the map
        	s.setY(groundHeight - s.getHeight());
        	inAir = false; 
        	
        }  
        sideCollision(s,elapsed);
         feetCollision(s); 
       
        if(s.getX()<10)
        {
          s.setX(10);
        }
        
        
                     
        
        // This will check for collision with feet
        
        
       
    }
   
    public void feetCollision(Sprite s)
    {     
        
        feetX = Math.floorDiv(((int)s.getX()-17-offset),tileWidth)+1; 
        feetY = Math.floorDiv(((int)(s.getY()+s.getHeight())),tileHeight);
        Tile tileFeet = tmap.getTile(feetX, feetY);
        System.out.println("feet tile "+feetX+" , "+(feetY)+" "+tmap.getTileChar(feetX, feetY));
       // System.out.println(s.getHeight());    
        if(tileFeet.getCharacter() == 'p')
        {
            int newGroundHeight = tileFeet.getYC();
            if (s.getY() + player.getHeight()>newGroundHeight)
                {            
        	   // Put the player back on the map    
                    //System.out.println("!!!");
                    s.setY(newGroundHeight - s.getHeight());
                    s.setVelocityY(-0.0006f);
                    inAir = false;        	
                 }           
          
        }      
        
         // System.out.println(s.getVelocityY());
    }
    public void sideCollision(Sprite s,long elapsed)
    {
        
        int noseX;
        int noseY = Math.floorDiv((int)(s.getY()+12),tileHeight);
        
        if(noseY<feetY)
        {
         // This checks for any collision with characters nose
        if(flapRight == true)
        {
          noseX = Math.floorDiv((int)(s.getX()-offset),tileWidth)+1;
        }
        else
        {
          noseX = Math.floorDiv((int)(s.getX()-offset-player.getWidth()+5),tileWidth)+1;
        }
        
        
        Tile t = tmap.getTile(noseX, noseY);
        
        
        if(t.getCharacter() == 'p' && flapRight == true)
        {
           
          noseColl = true;
          flapRight = false;
         /// s.setX(t.getXC()-s.getWidth()+offset);
          s.setVelocityX(-0.04f);  
          s.update(elapsed);
        }
        else if (t.getCharacter() == 'p'&& flapRight == false )
        {
        
          noseColl = true;
          flapLeft= false;
          //s.setX(t.getXC()+offset+tileWidth);
          s.setVelocityX(0.04f);   
          s.update(elapsed);
        }      
        else
        {
          noseColl = false;   
        }
        }
        
        
    }
    
     
    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     * 
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) 
    { 
    	int key = e.getKeyCode();
    	
    	if (key == KeyEvent.VK_ESCAPE) stop();
    	
    	if (key == KeyEvent.VK_UP && inAir==false && flapUp != true  )
        {      
          //  System.out.print("Hello");
            inAir = true;
            flapUp = true;
        }
        else
        {
            flapUp = false;
        }
    
        
        if(key == KeyEvent.VK_RIGHT && flapRight != true && noseColl == false && inAir == false )
        {    
            
            player.setInverse(false);
            flapRight = true;
           
        }
        
        if(key == KeyEvent.VK_LEFT  && flapLeft != true && inAir == false ) 
        {
            
            player.setInverse(true);
            flapLeft = true;           
        }
        
    	   	
    	if (key == KeyEvent.VK_S)
    	{
    		// Example of playing a sound as a thread
    		Sound s = new Sound("sounds/caw.wav");
    		s.start();
    	}
        e.consume();
    }

    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
        
        
    	return false;   	
    }


        @Override
	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		// Switch statement instead of lots of ifs...
		// Need to use break to prevent fall through.
		switch (key)
		{
			case KeyEvent.VK_ESCAPE : stop(); break;
			case KeyEvent.VK_UP     : flapUp = false; break;
                        case KeyEvent.VK_RIGHT : flapRight = false;break;
                        case KeyEvent.VK_LEFT : flapLeft = false;break;
			default :  break;
		}
	}
}
