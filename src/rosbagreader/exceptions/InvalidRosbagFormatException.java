/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rosbagreader.exceptions;

/**
 * Exception thrown when generic format error occurs while parsing the Rosbag file.
 * More info can be passed as a string message in the constructor.
 * 
 * @author Tomas Prochazka
 */
public class InvalidRosbagFormatException extends RosbagException{

    public InvalidRosbagFormatException(String message) {
        super(message);
    }
    

}
