/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rosbagreader;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 *ROS Time saved as seconds part and nanoseconds part.
 * @author Tomas Prochazka
 */
public class RosTime implements Comparable<RosTime>{
    private final int nsec;
    private final int sec;

    public RosTime(int nsec, int sec) {
        this.nsec = nsec;
        this.sec = sec;
        LocalTime t;
    }

    /**
     * Returns the nanoseconds component of the RosTime
     * (Number of nanoseconds since the "seconds" component.
     * @return 
     */
    public int getNsec() {
        return nsec;
    }

    /**
     * Gets the number of seconds since 1970.
     * @return 
     */
    public int getSec() {
        return sec;
    }

    public Date getDate() {
        return new Date(sec*1000L+nsec/1_000_000);
    }
    /**
     * Gets the time as a number of nanoseconds since 1970.
     * @return 
     */
    public long getTimeAsNanos() {
        return sec*1_000_000_000L+nsec;
    }
    public LocalDateTime getLocalDateTime(){
        Instant instant = Instant.ofEpochMilli(sec*1000L+nsec/1_000_000);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    @Override
    public int compareTo(RosTime o) {
        int c = sec-o.sec;
        if (c!=0) return c;
        return nsec-o.nsec;
    }
    
    
}
