import game2D.*;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.shape.Circle;
import javax.imageio.ImageIO;
import javax.sound.sampled.Line;

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
    float	gravity = 0.0009f;
    
    // Game state flags
    boolean flapUp = false;
    boolean flapRight = false;
    boolean flapLeft = false;
    boolean inAir = false;
    boolean directionRight = true;
    boolean noseColl = false;
    boolean feetColl = false;
    boolean dead = false;
    
    // Game resources
    Animation landing;
    Animation coin;
    Animation yoshiHatch;
    Animation deadMario;
    Animation heartAnim;
    Animation marioYoshi;
    Animation goomba;
    Sprite	player = null;
    Sprite yoshiHatchSprite = null;
    goomba goombaList;
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    Sprite coinSprite;
    Sprite heartSprite;
    
    //Sound object
    Sound bckSound;
    

    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
    Tile nearestTile;
    int tileWidth ;
    int tileHeight;
    int offset;
    int offsetY;
    long total;         			// The score will be the total time elapsed since a crash
    int feetY;
    int feetX;
    int feetPixX;
    int feetPixY;
    int bounce;
    int crack;
    int lives = 3;
    Rectangle chest; 
    BufferedImage bck;
    BufferedImage heart;
    int xo;
    int yo;
    
    Collision colHandler;

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
            try 
            {
                bck = ImageIO.read(new File("maps/Bck.png"));
                heart = ImageIO.read(new File("images/heart16.png"));
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
            
         //Load sound and play
        bckSound = new Sound("sounds/Super_Mario_Bros.wav");
       // bckSound.start();
        
        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map.txt");
        tileWidth = tmap.getTileWidth();
        tileHeight = tmap.getTileHeight();
        // Create a set of background sprites that we can 
        // rearrange to give the illusion of motion
   
        heartAnim = new Animation();
        heartAnim.loadAnimationFromSheet("images/heart16.png", 1,1, 1);
        landing = new Animation();
        landing.loadAnimationFromSheet("images/landbird.png", 5, 1, 60);
        yoshiHatch = new Animation();
        yoshiHatch.loadAnimationFromSheet("images/yoshiEgg.png", 6 ,1,60);
        deadMario = new Animation();
        deadMario.loadAnimationFromSheet("images/marioDead.png", 1, 1,1);
        marioYoshi = new Animation();
        marioYoshi.loadAnimationFromSheet("images/marioYoshi.png", 5, 1, 100);
       
       
        // Initialise the player with an animation
        player = new Sprite(landing,"mario");
        
        goombaList = new goomba(1);
        goombaList.addGoomba(96,320);
        
        coin = new Animation();
        coin.loadAnimationFromSheet("images/coinRed.png",4, 1, 60);           
        // Load a single cloud animation
        Animation ca = new Animation();
        ca.addFrame(loadImage("images/cloud.png"), 1000);
        
        colHandler = new Collision(tmap);
        // Create 3 clouds at random positions off the screen
        // to the right
        for (int c=0; c<3; c++)
        {
        	s = new Sprite(ca,"cloud");
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
        
        //127
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
        xo = (int)(64-player.getX()*(2048/512));
        yo = (int)(screenHeight-player.getHeight()-player.getY()-32);
   
        g.drawImage(bck, null,0,0);

        // Apply offsets to sprites then draw them
        for (Sprite s: clouds)
        {
        	s.setOffsets(xo,yo);
        	s.draw(g);
        }

        // Apply offsets to player and draw         
        player.setOffsets(xo, yo);
        if(coinSprite != null) 
        {
            coinSprite.setOffsets(xo, yo); 
            coinSprite.draw(g);
        }
        
        player.drawTransformed(g);
        
        if(yoshiHatchSprite!=null)
        {
          yoshiHatchSprite.setOffsets(xo, yo);
          yoshiHatchSprite.draw(g);
        }
        if(heartSprite!=null)
        {
          heartSprite.setOffsets(xo+5, yo);
          heartSprite.draw(g);
        }
        goombaList.offset(xo, yo);
        goombaList.draw(g);
        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo); 
        // Show score and status information
        String msg = String.format("Score: %d", total);
        g.setColor(Color.red);
        g.drawString(msg, getWidth() - 80, 50);
        for(int i = 0;i<lives;i++)
        {
          g.drawImage(heart, null,getWidth()-48-(i*16),53);
        }  
       
    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
        @Override
    public void update(long elapsed)
    {
    	
        // Make adjustments to the speed of the sprite due to gravity
        player.setVelocityY(player.getVelocityY()+(gravity*elapsed));
        if(lives == 0)dead=true;
        if(coinSprite != null)
        {
            coinSprite.setVelocityY(coinSprite.getVelocityY()+(gravity*elapsed));
            coinSprite.update(elapsed);
        }
        
        
    	offset = (int)(64-player.getX()*(2048/512));
        if(player.getDirectionRight() && player.getInAir() == false) // Do not allow movement mid jump
        {           
          player.playAnimation();
          player.setVelocityX(0.02f);
          player.setAnimationSpeed(1.0f);
          player.setDirectionRight(true);
        }         
       	if(player.getDirectionLeft() && player.getInAir() == false)
        {
            
           player.playAnimation();
           player.setVelocityX(-0.02f);
           player.setAnimationSpeed(1.0f);
           player.setDirectionRight(false);
        } 
        if(!player.getDirectionLeft() && !player.getDirectionRight() && player.getInAir() == false)
        {
            player.setVelocityX(0.0f);
            player.pauseAnimationAtFrame(2);
        }
       	if (player.getDirectionUp()) 
       	{     
            System.out.println("up");
            player.setVelocityY(-0.1f);                                 
       	}
  
       	for (Sprite s: clouds)
       		s.update(elapsed);
       	
        // Now update the sprites animation and position
        player.update(elapsed);
        goombaList.update(elapsed);
        colHandler.colUpdate(offset, yo);
        if(yoshiHatchSprite!=null)
        {
            yoshiHatchSprite.update(elapsed);
            yoshiHatchSprite.setVelocityY(yoshiHatchSprite.getVelocityY()+(gravity*elapsed));
        }
        if(heartSprite!=null)
        {
            heartSprite.update(elapsed);
            heartSprite.setVelocityY(heartSprite.getVelocityY()+(gravity*elapsed));
        }
        tmap.update(elapsed);
        // Then check for any collisions if alive
        if(!dead)handleTileMapCollisions(player,elapsed);
        else if(lives == 0) 
        {
            
            Sound s = new Sound("sounds/gameover.wav");
           // s.start();
//                bckSound.pause();
           
            player.setAnimation(deadMario);
            player.setVelocityY(-0.6f);
            player.setVelocityX(0);
            lives--;
        }
        
         	
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

        if ((s.getY() + player.getHeight()-yo)> groundHeight)
        {
        	// Put the player back on the map
        	s.setY(groundHeight - s.getHeight());
                s.setVelocityY(-0.01f);
        	player.setInAir(false);
        	
        } 
        char holder = colHandler.feetCollision(s,elapsed);
        if(holder=='f')
        {
            lives--; 
        }
        else if(holder == 'c')
        {
          total++;   
        }
        
        if(colHandler.headCollision(s,elapsed)=='m')mysteryBox((int)player.getX(),(int)player.getY(),elapsed);
        if(colHandler.sideCollision(s,elapsed)=='c')total++;
        else
        {
            colHandler.backHeadCollision(s,elapsed);
        }
        

        if(coinSprite != null)
        {  
          colHandler.coinCollision(coinSprite,elapsed);  
           if(boundingBoxCollision(s,coinSprite,elapsed))
           {
               coinSprite = null;
               total=total+20;
               Sound coinCollect = new Sound("sounds/smb_coin.wav");
              // coinCollect.start();
           }       
        }
        
        
        
        
        
        
        if(yoshiHatchSprite != null)
        {  
           colHandler.coinCollision(yoshiHatchSprite,elapsed);  
          
              if(yoshiHatchSprite.getAnimation().getFrameIndex()==5)
              {
                  yoshiHatchSprite.setType("yoshi");
                 if(boundingBoxCollision(s,yoshiHatchSprite,elapsed))
                     {
                         yoshiHatchSprite=null;
                         player.setAnimation(marioYoshi);
                     }
         
              }
              else if(boundingBoxCollision(s,yoshiHatchSprite,elapsed))
              {
                 yoshiHatchSprite.getAnimation().setLoop(false);
                 yoshiHatchSprite.playAnimation();  
              } 
        }
        
        
       if(heartSprite != null)
        {  
           colHandler.coinCollision(heartSprite,elapsed);  
           if(boundingBoxCollision(s,heartSprite,elapsed))
           {
               Sound sound = new Sound("sounds/life.wav");
               //sound.start();
               heartSprite = null;  
               lives++;
           }       
        }

    }
   // possible need to chest collision for in air colls  
   
    
    
    
    
     
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
    	
    	if (key == KeyEvent.VK_UP && player.getInAir()==false && player.getDirectionUp() != true  )
        {      
          //  System.out.print("Hello");
            Sound s = new Sound("sounds/jump.wav");
    	  //  s.start();
            player.setInAir(true);   
            player.setDirectionUp(true);
        }
        else
        {
            player.setDirectionUp(false);
        }
    
        
        if(key == KeyEvent.VK_RIGHT && player.getDirectionRight() != true && noseColl == false && player.getInAir() == false )
        {    
            
            player.setInverse(false);
            player.setDirectionRight(true);
           
        }
        
        if(key == KeyEvent.VK_LEFT  && player.getDirectionLeft() != true && player.getInAir() == false ) 
        {
            
            player.setInverse(true);
            player.setDirectionLeft(true);         
        }
        e.consume();
    }
/*
    *
    * Function determines type of sprite
    *
    *
    * s1 is always mario
    * s2 must be determined
    *
    * Detects collision between rectange and circe by splitting a rectange into 4 lines
    * if a the centre of the circle is less the the radius from any of the four lines
    * there has been a collision
    *
    */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2,long elapsed)
    {
        Circle coinCol; 
        Rectangle mario = new Rectangle((int)s1.getX()+3,(int)s1.getY()-yo,s1.getWidth()-7,s1.getHeight());
        Line2D marioBounds[] = rectToLines(mario);
        String type = s2.getType();
        switch(type)
        {
            case "coin":
                coinCol = new Circle((int)s2.getX()+8+offset,(int)s2.getY()+8,7);
                return(lineChecker(marioBounds,coinCol));
            case "heart":  
                coinCol = new Circle((int)s2.getX()+8+offset,(int)s2.getY()+8,7);
                return(lineChecker(marioBounds,coinCol));
            case "yoshiHatch":
                coinCol = new Circle((int)s2.getX()+5+offset,(int)s2.getY()+20,7);
                return(yoshiHatchCol(marioBounds,coinCol,elapsed));
            case "yoshi":
                Rectangle yoshi = new Rectangle((int)s2.getX()+offset,(int)s2.getY(),s2.getWidth()-10,s2.getHeight()-5);
                return((yoshiCol(s1,yoshi,elapsed)));
              
        }
   
    	return false;   	
    }
    /*
    * To determine if mario has collide with the circle Egg
    * and to handle the collision
    */
    public boolean yoshiHatchCol(Line2D[] marioBounds,Circle circleCol,long elapsed)
    {
        for(int i = 0;i<4;i++)
             {
                 if(marioBounds[i].ptSegDist(circleCol.getCenterX(),circleCol.getCenterY())<7 )
                 {               
                     if(i==1)
                     {
                         player.setCol(true);
                         player.setDirectionRight(false);
                         player.setVelocityX(-0.03f);  
                         player.update(elapsed);
                          
                     }
                     else if(i==2)
                     {
                         player.setVelocityY(-0.1f);
                         player.update(elapsed);
                     }
                     else if(i==3)
                     {
                         player.setCol(true);
                         player.setDirectionLeft(true);                       
                         player.setVelocityX(0.03f);   
                         player.update(elapsed);
                        
                     }
                     return true;
                     }
                    
                 }
               return false;  
               
                   
    }
    // To determine if mario has collided with yoshi
    // Mario can only ride if he jumps on on collides with the left
    public boolean yoshiCol(Sprite s1,Rectangle r,long elapsed)
    {
        Rectangle mario = new Rectangle((int)s1.getX()-3,(int)s1.getY()-yo,s1.getWidth()-7,s1.getHeight()-5);
        if(mario.intersects(r))
        {  
           if(player.getDirectionRight() && s1.getX()<r.getX())
           {
                return true;
           }
           else if(player.getDirectionLeft())
           {
                player.setCol(true);
                player.setDirectionLeft(false);
               // s.setX(t.getXC()+offset+tileWidth);
                player.setVelocityX(0.03f);   
                player.update(elapsed);
                
           }
           else if(player.getInAir())
           {
              return true;
           }
          
        }
        return false;
        
    }
    // Takes 4 lines off a rectangle and determines the nearest point on 
    // each line to the centre of the given circle if the distance is less
    // than the diametre than the rectangle must be colliding with the circle
    public boolean lineChecker(Line2D[] marioBounds,Circle circleCol)
    {
       for(int i = 0;i<4;i++)
       {
           if(marioBounds[i].ptSegDist(circleCol.getCenterX(),circleCol.getCenterY())<7 )
            {               
                return true;
            }
       }
        return false;   
    }
    public Line2D[] rectToLines(Rectangle r)
    {
        Line2D[] lines = new Line2D.Double[4];
        lines[0]= new Line2D.Double(r.getX(),r.getY(), r.getX()+r.getWidth(),r.getY());
        lines[1]= new Line2D.Double(r.getX()+r.getWidth(),r.getY(),r.getX()+r.getWidth(),r.getY()+r.getHeight());
        lines[2] = new Line2D.Double(r.getX()+r.getWidth(),r.getY()+r.getHeight(),r.getX(),r.getY()+r.getHeight());
        lines[3] = new Line2D.Double(r.getX(),r.getY()+r.getHeight(),r.getX(),r.getY());
        
        return lines;
    }


        @Override
	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		// Switch statement instead of lots of ifs...
		// Need to use break to prevent fall through.
		switch (key)
		{
			case KeyEvent.VK_ESCAPE : stop(); break;
			case KeyEvent.VK_UP     : player.setDirectionUp(false); break;
                        case KeyEvent.VK_RIGHT : player.setDirectionRight(false);break;
                        case KeyEvent.VK_LEFT : player.setDirectionLeft(false);break;
			default :  break;
		}
	}
  
        public void mysteryBox(int x,int y,long elapsed)
        {
            Sprite[] boxSprites = new Sprite[3];            
          coinSprite = new Sprite(coin,"coin");
          coinSprite.setAnimationSpeed(0.5f);
          boxSprites[0] = coinSprite;
          heartSprite = new Sprite(heartAnim,"heart");
          boxSprites[1] = heartSprite;
          yoshiHatchSprite = new Sprite(yoshiHatch,"yoshiHatch");
           yoshiHatchSprite.pauseAnimation();

          boxSprites[2] = yoshiHatchSprite;
          
          Random randomGenerator = new Random();
          
          mysteryAnimation(x,y,elapsed,boxSprites[randomGenerator.nextInt(3)]);
            
        }
    public void mysteryAnimation(int x,int y,long elapsed,Sprite sprite)
    {
        
        
        sprite.setX(x);
        sprite.setY(y);
        sprite.setAnimationFrame(0);
        sprite.show();
        sprite.setVelocityY(-0.5f);
   
        
    }
    
 
    
        
        
}
