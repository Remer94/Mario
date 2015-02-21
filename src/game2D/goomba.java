/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package game2D;

import java.awt.Graphics2D;
import java.awt.List;
import java.util.ArrayList;

/**
 *
 * @author ryanremer
 */
public class goomba 
{
    Sprite s[];
    int count = 0;
    ArrayList<Sprite> goombaDraw;
    static int screenWidth = 512;
    static int screenHeight = 384;
    Animation goombaAnim; 
    public goomba(int quantity)
    {
        goombaAnim = new Animation();
        goombaAnim.loadAnimationFromSheet("maps/goomba.png", 2, 1,50);
        goombaAnim.setAnimationSpeed(0.05f);
        
        s = new Sprite[quantity];  
        goombaDraw = new ArrayList<Sprite>();
    }
    public void addGoomba(int x,int y)
    {
      s[count] = new Sprite(goombaAnim,"goomba");
      s[count].setX(x);
      s[count].setY(y);
      s[count].show();
      count++;
    }
    // returns a list off all goombas currently on screen
    public Sprite[] toDraw()
    {
       for(Sprite goomba:s)
       {
           if(goomba.getX()<screenWidth && goomba.getY()>0)
           {
               goombaDraw.add(goomba);
           }
       }
       return goombaDraw.toArray(new Sprite[goombaDraw.size()]);
               
    }
    // only draw goombas that appear in screen dimensions
    public void draw(Graphics2D g)
    {
       // System.out.println("drawing");
        for(Sprite goomba:toDraw())
        {        
            goomba.draw(g);
        }
    }
    // apply offset to all goombas
    public void offset(int ofX,int ofY)
    {
        for(Sprite goomba:s)
        {
            goomba.setOffsets(ofX, ofY);
        }
    }
    // update all goombas on screen
    public void update(long elapsed)
    {
        for(Sprite goomba:toDraw())
        {
            goomba.update(elapsed);
        }
    }
}
