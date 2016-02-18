/*
 */

package rosbagreader;

/**
 *See: http://docs.ros.org/api/std_msgs/html/msg/Header.html
 * @author Tomas Prochazka
 * 21.11.2015
 */
public class RosStandardMessageHeader {
    /**
     * Sequence ID
     */
    public long seq;
    /**
     * Time stamp of the message
     */
    public RosTime stamp;
    /**
     * Frame this data is associated with
     */
    public String frameId;
}
