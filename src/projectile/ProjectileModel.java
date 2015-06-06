package projectile;
/**
 * Mathematical model of projectile, calculates its position*
 * @author Varsha
 * @version 6/6/15
 */
public class ProjectileModel
{
    private double currX,currY,initialX,initialY;
    private double currVX,currVY,initialVX,initialVY;
    private double currAX,currAY,gravity;
    private double initialAngle,initialV;
    private double timeElapsed;
    /**
     * Constructs an instance of the projectile
     * @param x initial X coordinate
     * @param y initial Y coordinate
     * @param angle initial angle with respect to horizontal
     * @param v initial speed (combines horizontal and vertical components)
     */
    public ProjectileModel(double x, double y, double angle, double v)
    {
        initialX = x;
        initialY = y;
        initialV = v;
        initialAngle = Math.toRadians(angle);
    }

    /**
     * sets acceleration from gravity, calculates horizontal and vertical components
     * of speed through initial velocity and initial angle
     */
    public void initialize()
    {
        //gravity is positive in this simulation, increase in Y means down
        currAY = gravity;
        //calculates horizontal component using trig
        initialVX = initialV*Math.cos(initialAngle);
        //trig again, note that velocity is negative because upwards is decrease in Y
        initialVY = -initialV*Math.sin(initialAngle);
    }

    /**
     * loads current location of projectile
     */
    public void fire()
    {
        currX = initialX;
        currY = initialY;
    }

    /**
     * updates location using physics calculations
     * @param timeMili time interval between successive calls of this method
     */
    public void step(int timeMili)
    {
        timeElapsed+=timeMili;
        //converts miliseconds to seconds
        double timeSec = timeElapsed/1000.0;
        //physics equation: location = d0 + v0*t + 1/2*a*t^2
        currX = initialX + initialVX*timeSec + currAX*timeSec*timeSec*0.5;
        currY = initialY + initialVY*timeSec + currAY*timeSec*timeSec*0.5;
    }

    /**
     * this method is not used in the current version, but adds expandability
     * @param x horizontal component of initial location
     * @param y vertical component
     */
    public void setPos(double x, double y)
    {
        initialX=x;
        initialY=y;
    }

    /**
     * calculates altitude with respect to initial level
     * @return the altitude
     */
    public double getAltitude(){return initialY-currY;}

    /**
     * sets the value of gravity
     * @param g gravity in m/s^2
     */
    public void setG(double g){gravity = g;}

    /**
     * access method
     * @return current X component
     */
    public double getX(){return currX;}

    /**
     * access method
     * @return current Y component
     */
    public double getY(){return currY;}

    /**
     * access method for time
     * @return time elapsed in seconds
     */
    public double getTimeElapsed(){return timeElapsed/1000;}

    /**
     * calculates range of projectile
     * @return range in meters
     */
    public double getRange(){ return currX-initialX;}
}
