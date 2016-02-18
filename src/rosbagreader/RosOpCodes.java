/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rosbagreader;

/**
 *Defines constants for ROS Bag opcodes
 *as they are described on http://wiki.ros.org/Bags/Format/2.0#Op_codes
 * 
 * @author Tomas Prochazka
 */
public class RosOpCodes {
   private RosOpCodes () {
       
   }
    public static final int BAG_HEADER=0x03;
    public static final int CHUNK=0x05;
    public static final int CONNECTION=0x07;
    public static final int MESSAGE_DATA=0x02;
    public static final int INDEX_DATA=0x04;
    public static final int CHUNK_INFO=0x06;
    
    
}
