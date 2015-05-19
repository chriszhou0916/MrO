import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.*;
/**
 * Abstract class FlyingObject - write a description of the class here
 * 
 * @author (your name here)
 * @version (version number or date here)
 */
public class FlyingObject extends JComponent
{
    private Model m;
    public FlyingObject()
    {
        m = new Model(300,300,45,100);
        m.setG(9.8);
    }
    
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        Ellipse2D.Double ball = new Ellipse2D.Double(m.getX(),m.getY(),10,10);
        g2.draw(ball);
        g2.fill(ball);
    }
    
    public void move(int s)
    {
        m.step(s);
        repaint();
    }
}
