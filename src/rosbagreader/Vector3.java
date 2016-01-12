/*
 */

package rosbagreader;

/**
 * Represents a vector in 3 dimensional space.
 * Used for storing of ROS geometry_msgs/Vector3.
 * However, the structure is equivalent also to geometry_msgs/Point.
 * @author Tomas Prochazka
 * 12.1.2016
 */
public class Vector3 {

    public Vector3() {
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public double x;
    public double y;
    public double z;
    
}
