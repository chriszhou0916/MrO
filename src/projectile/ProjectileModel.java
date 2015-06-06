package projectile;
/**
 * Write a description of class FlyingThing here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class ProjectileModel
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
    public ProjectileModel(double x, double y, double angle, double v)
    {
        initialX = x;
        initialY = y;
        initialV = v;
        initialAngle = Math.toRadians(angle);
    }

    /**
     *
     */
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

        currX = initialX + initialVX*timeSec + currAX*timeSec*timeSec*0.5;
        currY = initialY + initialVY*timeSec + currAY*timeSec*timeSec*0.5;
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

    public double getAltitude(){return initialY-currY;}

    public void setG(double g){gravity = g;}

    public double getX(){return currX;}

    public double getY(){return currY;}

    public double getTimeElapsed(){return timeElapsed/1000;}

    public double getRange(){ return currX-initialX;}

    public boolean getIsFlying(){return isFlying;}
}
