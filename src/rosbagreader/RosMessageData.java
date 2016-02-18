/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rosbagreader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import rosbagreader.exceptions.UnexpectedEndOfRosbagFileException;
import support.ReaderSupport;
import rosbagreader.exceptions.UnexpectedEndOfFileException;

/**
 * Provides access to the data part of the message record. Contains all
 * information about the message currently read.
 *
 * @author Tomas Prochazka
 */
public class RosMessageData {

    private final int length;
    private int bytesLeft;
    private final InputStream inputStream;
    private final ReaderSupport readerSupport = new ReaderSupport();
    /**
     * Headers of the message data record. The header fields are stored as a
     * (String fieldName)->(byte[] fieldValue) map.
     */
    private final Map<String, byte[]> header;
    private final String topic;
    private final RosTime time;

    public RosMessageData(int recordLength, InputStream inputStream, Map<String, byte[]> header, String topicName, RosTime messageTime) {
        this.length = recordLength;
        this.inputStream = inputStream;
        this.header = header;
        this.topic = topicName;
        this.time = messageTime;
        this.bytesLeft = length;
    }

    /**
     * Size of the message in bytes.
     *
     * @return
     */
    public int getRecordLength() {
        return length;
    }

    /**
     * Number of bytes that haven't been read yet.
     *
     * @return
     */
    public int getBytesLeft() {
        return bytesLeft;
    }

    /**
     * Reads an unsigned byte (0..255) from the ROSBbag message data.
     *
     * @return 0..255
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException
     */
    public int readByte() throws IOException, UnexpectedEndOfRosbagFileException {
        if (bytesLeft < 1) {
            throw new IllegalStateException("Ros Message input stream is empty. Can't read byte.");
        }
        int res;
        res = inputStream.read();
        if (res == -1) {
            throw new UnexpectedEndOfRosbagFileException();
        }
        bytesLeft -= 1;
        return res;
    }

    /**
     * Reads 4-byte integer from the ROSBbag message data.
     *
     * @return
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException
     */
    public int readInt() throws IOException, UnexpectedEndOfRosbagFileException {
        if (bytesLeft < 4) {
            throw new IllegalStateException("There are not enough data left in the ROS Message to read int.");
        }
        int res;
        try {
            res = readerSupport.readLittleEndianInt(inputStream);
        } catch (UnexpectedEndOfFileException ex) {
            throw new UnexpectedEndOfRosbagFileException(ex);
        }
        bytesLeft -= 4;
        return res;
    }

    /**
     * Reads 8-bytes long integer from the ROSBag file.
     *
     * @return
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException
     */
    public long readLong() throws IOException, UnexpectedEndOfRosbagFileException {
        if (bytesLeft < 8) {
            throw new IllegalStateException("There are not enough data left in the ROS Message to read long.");
        }
        long res;
        try {
            res = readerSupport.readLittleEndianLong(inputStream);
        } catch (UnexpectedEndOfFileException ex) {
            throw new UnexpectedEndOfRosbagFileException(ex);
        }
        bytesLeft -= 8;
        return res;
    }

    /**
     * Reads 4 bytes from the message input stream and interprets them as
     * unsigned int.
     *
     * @return
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException
     */
    public long readUnsignedInt() throws IOException, UnexpectedEndOfRosbagFileException {
        int intVal = readInt();
        return intVal & 0xFF_FF_FF_FFL;
    }

    /**
     * Reads string from the ROSBag message. Warning: The serialization format
     * of the string was guessed. (Although based on: http://wiki.ros.org/msg)
     *
     * @return
     * @throws java.io.IOException
     * @throws rosbagreader.exceptions.UnexpectedEndOfRosbagFileException
     */
    public String readString() throws IOException, UnexpectedEndOfRosbagFileException {
        int len = readInt();
        byte[] bytes = readBytes(len);
        return readerSupport.bytesToString(bytes);
    }

    /**
     * Reads a float array from the ROSBag message.
     * @return
     * @throws java.io.IOException
     * @throws rosbagreader.exceptions.UnexpectedEndOfRosbagFileException
     */
    public float[] readFloatArray() throws IOException, UnexpectedEndOfRosbagFileException {
        int len = readInt();
        float[] res = new float[len];
        for (int i = 0; i < len; i++) {
            res[i] = readFloat();
        }
        return res;
    }
    /**
     * Reads a double array from the message.
     * @return
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException 
     */
    public double[] readDoubleArray() throws IOException, UnexpectedEndOfRosbagFileException {
        int len = readInt();
        double[] res = new double[len];
        for (int i = 0; i < len; i++) {
            res[i]=readDouble();
            
        }
        return res;
    }

    /**
     * Reads an array of 32-bit unsigned integers from the ROSBag message.
     *
     * @return
     * @throws java.io.IOException
     * @throws rosbagreader.exceptions.UnexpectedEndOfRosbagFileException
     */
    public long[] readUnsignedIntArray() throws IOException, UnexpectedEndOfRosbagFileException {
        int len = readInt();
        long[] res = new long[len];
        for (int i = 0; i < len; i++) {
            res[i] = readUnsignedInt();
        }
        return res;
    }

    /**
     * Reads a sub-message of type: std_msgs/Header which contains a sequence
     * number, time stamp and frame id. See:
     * http://docs.ros.org/api/std_msgs/html/msg/Header.html
     *
     * @return
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException
     */
    public RosStandardMessageHeader readMessageHeader() throws IOException, UnexpectedEndOfRosbagFileException {
        RosStandardMessageHeader h = new RosStandardMessageHeader();
        h.seq = readUnsignedInt();
        h.stamp = readTime();
        h.frameId = readString();
        return h;
    }

    /**
     * If the message has not been read to end, the unread bytes will be
     * skipped.
     *
     * @throws IOException
     */
    void finish() throws IOException, UnexpectedEndOfRosbagFileException {
        if (bytesLeft > 0) {
            if (!readerSupport.skipExactCount(inputStream, bytesLeft)) {
                throw new UnexpectedEndOfRosbagFileException("End of file found while skipping message data bytes.");
            }
        }
        bytesLeft = 0;
    }

    /**
     * Get message header fields
     *
     * @return
     */
    public Map<String, byte[]> getHeader() {
        return header;
    }

    /**
     * Get message topic.
     *
     * @return
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Get message time.
     *
     * @return
     */
    public RosTime getTime() {
        return time;
    }

    /**
     * Reads the given number of bytes from the message. e.g.
     * message.readBytes(message.getBytesLeft()) will return the rest of the
     * message as an array of bytes.
     *
     * @param bytesCount Number of bytes to read.
     * @return
     * @throws UnexpectedEndOfRosbagFileException
     * @throws IOException
     */
    public byte[] readBytes(int bytesCount) throws UnexpectedEndOfRosbagFileException, IOException {
        if (bytesCount > bytesLeft) {
            throw new IllegalStateException("There are not enough data in the messsage.");
        }
        byte[] bytes = new byte[bytesCount];

        if (bytesCount != inputStream.read(bytes)) {
            throw new UnexpectedEndOfRosbagFileException();
        }
        //subtracts the read amount of bytes from bytesLeft.
        bytesLeft -= bytesCount;
        return bytes;
    }

    RosTime readTime() throws IOException, UnexpectedEndOfRosbagFileException {
        int sec = readInt();
        int nsec = readInt();
        return new RosTime(nsec, sec);
    }

    public float readFloat() throws IOException, UnexpectedEndOfRosbagFileException {
        if (bytesLeft < 4) {
            throw new IllegalStateException("There are not enough data left in the ROS Message to read a float.");
        }
        float f;
        try {
            f = readerSupport.readLittleEndianFloat(inputStream);
        } catch (UnexpectedEndOfFileException ex) {
            throw new UnexpectedEndOfRosbagFileException(ex);
        }
        bytesLeft -= 4;
        return f;
    }

    /**
     * Read 8 bytes from the message, interprets them as double (float64)
     * @return
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException 
     */
    public double readDouble() throws IOException, UnexpectedEndOfRosbagFileException {
        if (bytesLeft < 8) {
            throw new IllegalStateException("There are not enough data left in the ROS Message to read a float.");
        }
        double d;
        try {
            d = readerSupport.readLittleEndianDouble(inputStream);
        } catch (UnexpectedEndOfFileException ex) {
            throw new UnexpectedEndOfRosbagFileException("Unexpected end of file while reading double from the message.");
        }
        bytesLeft-=8;
        return d;
    }
    /**
     * Reads the sub-message of type geometry_msgs/Vector3 from the message data.
     * @return 
     * @throws java.io.IOException 
     * @throws rosbagreader.exceptions.UnexpectedEndOfRosbagFileException 
     */
    public Vector3 readVector3() throws IOException, UnexpectedEndOfRosbagFileException {
        return new Vector3(readDouble(), readDouble(),readDouble());
    } 

}
