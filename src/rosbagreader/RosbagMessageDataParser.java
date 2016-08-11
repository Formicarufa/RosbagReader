/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rosbagreader;

import java.io.IOException;
import rosbagreader.exceptions.UnexpectedEndOfRosbagFileException;

/**
* An interface that the user of the RosbagReader class needs to implement
 * @author Tomas Prochazka
 */
public interface RosbagMessageDataParser {
    /**
     * Interface that the user of the RosbagReader class needs to implement
     * to process the data in the messages of the ROSBag file.
     * The user can read entries from        the message using methods such as
     * message.readInt()
     * as long as there are bytes left.
     * (Number of bytes left can be obtained by calling message.getBytesLeft().);
     * @param message Provides access to the message content and its header.
     * @throws java.io.IOException
     * @throws rosbagreader.exceptions.UnexpectedEndOfRosbagFileException
     */
    void parseMessageData(RosMessageData message) throws IOException,UnexpectedEndOfRosbagFileException;
}
