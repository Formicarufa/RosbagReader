/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rosbagreader;

/**
 *Contains names of the fields that are required in some of the record types.
 * @author Tomas Prochazka
 */
public class RosbagHeaderFieldNames {
    /**
     * Op field is required for all records.
     * List of possible values as constants can be found in the RosOpCodes class.
     */
    public static final String OP = "op";
    /**
     * This is a required field for both connection and message-data record type header.
     * Value of this field is an 4-bytes integer - unique connection ID
     */
    public static final String CONN="conn";
    /**
     * Value of this connection header field is a string which contains the topic name.
     */
    public static final String TOPIC = "topic";
    
    /**
     * Must be in the message-data header.
     * Value type: int64
     */
    public static final String TIME = "time";
    
    /**
     * Each chunk record header needs to have this field.
     */
    public static final String COMPRESSION="compression";
    
    

}
