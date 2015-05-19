
/**
 * Write a description of class FlyingThing here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Model
{
    private double currX,currY,initialX,initialY;
    private double currVX,currVY,initialVX,initialVY;
    private double currAX,currAY,gravity;
    private double initialAngle,initialV;
    private double mass,diameter;
    private double altitude;
    private double timeElapsed;
    private boolean isFlying;
    private String item;
    public Model(double x, double y, double angle, double v)
    {
        initialX = x;
        initialY = y;
        initialV = v;
        initialAngle = angle;        
    }
    
    public void initialize()
    {
        currAY = gravity;
        initialVX = initialV*Math.cos(initialAngle);
        initialVY = -initialV*Math.sin(initialAngle);
    }
    
    public void fire()
    {
        currX = initialX;
        currY = initialY;
        isFlying = true;
    }

    public void step(int timeMili)
    {
        timeElapsed+=timeMili;
        double timeSec = timeElapsed/1000.0;
        System.out.println(timeSec);

        currX = initialX + initialVX*timeSec + currAX*timeSec*timeSec*0.5;
        currY = initialY + initialVY*timeSec + currAY*timeSec*timeSec*0.5;
        altitude = altitude+initialVY*timeSec + altitude*timeSec*timeSec*0.5;
        if(altitude<=0)
        {
            isFlying=false;
        }
    }

    public void setPos(double x, double y)
    {
        initialX=x;
        initialY=y;
    }

    public void setG(double g){gravity = g;}

    public double getX(){return currX;}

    public double getY(){return currY;}
}
