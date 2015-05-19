
/**
 * Write a description of class AppFrame here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class AppFrame extends JFrame
{
    public static final int FRAME_WIDTH = 720,FRAME_HEIGHT = 1280;
    public AppFrame()
    {
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        
        setTitle("Projectile Motion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final FlyingObject a = new FlyingObject();
        add(a);
        final int DELAY = 50;
        setVisible(true);
        class TimerListener implements ActionListener
        {
            public void actionPerformed(ActionEvent event)
            {
                a.move(50);
            }
        }

        ActionListener listener = new TimerListener();

        
        Timer t = new Timer(DELAY, listener);
        t.start();
        
    }
}
