/**
 *
 */
package rosbagreader;

import java.io.BufferedInputStream;
import support.ReaderSupport;
import rosbagreader.exceptions.RequiredFieldMissingRosbagException;
import rosbagreader.exceptions.UnexpectedEndOfRosbagFileException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import rosbagreader.exceptions.InvalidFieldValueRosbagException;
import rosbagreader.exceptions.InvalidRosbagFormatException;
import rosbagreader.exceptions.UnexpectedEndOfFileException;

/**
 * Reads the structure of the ROS Bag file.
 * Does not read the message content.
 * User must provide RosbagMessageDataParser
 * which reads the message content based on 
 * topic and size of the message.
 * @author Tomas Prochazka
 *
 */
public class RosbagReader {

    //Possible optimization for huge files: instead of using buffered input stream do buffering directly: reading could be even twice as fast.
    //(According to: http://www.oracle.com/technetwork/articles/javase/perftuning-137844.html#)
    // However, this would make the code much less readable.
    private final InputStream input;
    private final ReaderSupport readerSupport = new ReaderSupport();

    /**
     * Creates a new rosbag reader. Opens the file and reads it's headers.
     *
     * @param input
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException
     * @throws InvalidRosbagFormatException
     * @throws RequiredFieldMissingRosbagException
     * @throws InvalidFieldValueRosbagException
     */
    public RosbagReader(InputStream input) throws IOException, UnexpectedEndOfRosbagFileException, InvalidRosbagFormatException, RequiredFieldMissingRosbagException, InvalidFieldValueRosbagException {
        this.input = new BufferedInputStream(input);
        verifyFirstLine();
        readRosbagHeader();
    }

    /**
     * Checks that the rosbag file starts with the text: #ROSBAG V2.0 If not, an
     * exception is thrown.
     *
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException
     * @throws InvalidRosbagFormatException
     */
    private void verifyFirstLine() throws IOException, UnexpectedEndOfRosbagFileException, InvalidRosbagFormatException {
        String firstLine = "#ROSBAG V2.0\n";
        for (int i = 0; i < firstLine.length(); i++) {
            char c = firstLine.charAt(i);
            int code = (int) c;
            int r = readNotEndOfFile();
            if (r != c) {
                throw new InvalidRosbagFormatException("Rosbag file should start with the text:" + firstLine);
            }
        }

    }

    /**
     * Reads the record header and returns the fields as a name -> value hash map.
     *
     * @param bytesCount Length of the header
     * @return map of field names and field values
     * @throws UnexpectedEndOfRosbagFileException
     * @throws IOException
     * @throws InvalidRosbagFormatException
     */
    public Map<String, byte[]> readHeader(int bytesCount) throws UnexpectedEndOfRosbagFileException, IOException, InvalidRosbagFormatException {
        HashMap<String, byte[]> map = new HashMap<>();

        while (bytesCount > 0) {
            int fieldLength = readInt();
            bytesCount -= 4+fieldLength; //-4 bytes for the fieldLength information itself 
            //read the field's name
            StringBuilder name = new StringBuilder();
            for (char c = (char) readNotEndOfFile(); c != '='; c = (char) readNotEndOfFile()) {
                name.append(c);
            }
            int valueLength = fieldLength - name.length() - 1; //-1 is for the '=' sign.   

            byte[] value;
            try {
                value = new byte[valueLength];
            } catch (NegativeArraySizeException e) {
                throw new InvalidRosbagFormatException("Header field length is less than field name length.");
            }
            if (valueLength != input.read(value)) {
                throw new UnexpectedEndOfRosbagFileException();
            }
            map.put(name.toString(), value);
        }
        if (bytesCount < 0) {
            throw new InvalidRosbagFormatException("Header length is less then the length of all header fields.");
        }
        return map;

    }

    private int readInt() throws IOException,UnexpectedEndOfRosbagFileException {
        int result;
        try {
            result = readerSupport.readLittleEndianInt(input);
        } catch (UnexpectedEndOfFileException ex) {
            throw new UnexpectedEndOfRosbagFileException(ex);
        }
        return result;
    }

    /**
     * Reads one byte from the input file.
     *
     * @return the read byte value
     * @throws UnexpectedEndOfRosbagFileException if end of file was reached
     * @throws IOException
     */
    private int readNotEndOfFile() throws UnexpectedEndOfRosbagFileException, IOException {
        int b = input.read();
        checkEndOfFile(b);
        return b;
    }

    /**
     * Throws an exception if the given parameter is equal to -1
     *
     * @param b integer, value -1 causes that an exception is thrown.
     * @throws UnexpectedEndOfRosbagFileException
     */
    private void checkEndOfFile(int b) throws UnexpectedEndOfRosbagFileException {
        if (b == -1) {
            throw new UnexpectedEndOfRosbagFileException();
        }
    }

    Map<Integer, String> topics = new HashMap<>();

    public void parseBag(RosbagMessageDataParser parser) throws IOException, UnexpectedEndOfRosbagFileException, InvalidRosbagFormatException, RequiredFieldMissingRosbagException, InvalidFieldValueRosbagException {
       //TODO: to increase efficiency, rework so that BufferedInputStream can be used.
        ReaderSupport.IntWrapper messageSize = new ReaderSupport.IntWrapper();
        while (tryReadLEInteger(input, messageSize)) {
            //Possible solution: write method: bool readIntOrNothing(Integer result)
            Map<String, byte[]> header = readHeader(messageSize.i); // Passes the header size to the readHeader method
            int op = getOpCode(header);
            String topic;
            switch (op) {
                case RosOpCodes.CHUNK:
                    parseChunkRecord(header, parser);
                    break;
                case RosOpCodes.INDEX_DATA:
                case RosOpCodes.CHUNK_INFO:
                    //Metadata records are ignored
                    int count = readInt();
                    skipBytes(count);
                    break;
                case RosOpCodes.BAG_HEADER:
                    throw new InvalidRosbagFormatException("Bag header shouldn't be present more then once (Only as the first record in the document).");
                case RosOpCodes.CONNECTION:
                    parseConnection(header, topics);
                    break;
                case RosOpCodes.MESSAGE_DATA:
                    parseMessageRecord(header, parser,topics);
                    break;
            }

        }
    }
    /**
     * Decodes the topic and time information from the message header
     * and calls the parser to parse the message.
     * @param header 
     * @param parser
     * @throws InvalidRosbagFormatException
     * @throws RequiredFieldMissingRosbagException
     * @throws IOException
     * @throws InvalidFieldValueRosbagException
     * @throws UnexpectedEndOfRosbagFileException 
     * @return number of bytes read
     */
    private int parseMessageRecord(Map<String, byte[]> header, RosbagMessageDataParser parser, Map<Integer,String> currentTopics) throws InvalidRosbagFormatException, RequiredFieldMissingRosbagException, IOException, InvalidFieldValueRosbagException, UnexpectedEndOfRosbagFileException {
        String topic;
        int topicId = getConnectionId(header);
        if (!currentTopics.containsKey(topicId)) throw new InvalidRosbagFormatException("Topic with the connection id "+ topicId + " was not declared in the connection record.");
        topic = currentTopics.get(topicId);
        int bytesCount = readInt();
        RosTime time = getTime(header);
        RosMessageData message = new RosMessageData(bytesCount, input, header, topic, time);
        parser.parseMessageData(message);
        message.finish();
            
       
        return  4+bytesCount;   
    }

    /**
     * Reads the topic from the header field. Throws an exception if the header
     * field is not present.
     *
     * @param header Connection record header.
     * @return string containing the message topic name.
     * @throws RequiredFieldMissingRosbagException
     */
    private String getConnectionTopic(Map<String, byte[]> header) throws RequiredFieldMissingRosbagException {
        if (!header.containsKey(RosbagHeaderFieldNames.TOPIC)) {
            throw new RequiredFieldMissingRosbagException("Each connection record must contain the 'topic' header field.");
        }
        byte[] topicBytes = header.get(RosbagHeaderFieldNames.TOPIC);
        String topic = new String(topicBytes, StandardCharsets.UTF_8); //only characters from ASCII 0 - 127 are expected.
        return topic;
    }

    /**
     * Reads the connection id from the header field. Throws an exception if the
     * header field is not present of if it has an invalid format.
     *
     * @param header Header of a connection record or message-data record.
     * @return Unique connection ID.
     * @throws InvalidFieldValueRosbagException
     * @throws RequiredFieldMissingRosbagException
     */
    private int getConnectionId(Map<String, byte[]> header) throws InvalidFieldValueRosbagException, RequiredFieldMissingRosbagException {
        if (!header.containsKey(RosbagHeaderFieldNames.CONN)) {
            throw new RequiredFieldMissingRosbagException("Each connection and message-data record must contain the 'conn' header field.");
        }
        byte[] idBytes = header.get(RosbagHeaderFieldNames.CONN);
        if (idBytes.length != 4) {
            throw new InvalidFieldValueRosbagException("Connection 'conn' field should have 4 bytes long value.");
        }
        int connId = readerSupport.readLittleEndianInt(idBytes);
        return connId;
    }

    /**
     *
     * @param header
     * @return
     * @throws InvalidFieldValueRosbagException
     * @throws RequiredFieldMissingRosbagException
     */
    private int getOpCode(Map<String, byte[]> header) throws InvalidFieldValueRosbagException, RequiredFieldMissingRosbagException {
        if (!header.containsKey(RosbagHeaderFieldNames.OP)) {
            throw new RequiredFieldMissingRosbagException("Each record must have 'op' field.");
        } else {
        }
        byte[] opBytes = header.get(RosbagHeaderFieldNames.OP);
        if (opBytes.length != 1) {
            throw new InvalidFieldValueRosbagException("Op field's value must be 1 byte long");
        }
        int op = readerSupport.byteAsUnsigned(opBytes[0]);
        return op;
    }

    private Map<String, byte[]> bagHeader;

    /**
     * The meta-data of the opened ROS Bag file as a [name -> byte value]
     * hashmap. It is initialized and loaded in the constructor of the RosbagReader.
     *
     * @return
     */
    public Map<String, byte[]> getBagHeader() {
        return bagHeader;
    }

    /**
     * Reads the bag header. The bag header must be the first record in the
     * file.
     *
     * @throws UnexpectedEndOfRosbagFileException
     * @throws IOException
     * @throws InvalidRosbagFormatException
     * @throws RequiredFieldMissingRosbagException
     * @throws InvalidFieldValueRosbagException
     */
    private void readRosbagHeader() throws UnexpectedEndOfRosbagFileException, IOException, InvalidRosbagFormatException, RequiredFieldMissingRosbagException, InvalidFieldValueRosbagException {
        bagHeader = readHeader(readInt());
        if (!bagHeader.containsKey(RosbagHeaderFieldNames.OP)) {
            throw new RequiredFieldMissingRosbagException("op header field missing");
        }
        byte[] op = bagHeader.get(RosbagHeaderFieldNames.OP);
        if (op.length != 1) {
            throw new InvalidFieldValueRosbagException("op value should be only one byte long");
        }
        if (readerSupport.byteAsUnsigned(op[0]) != RosOpCodes.BAG_HEADER) {
            throw new InvalidRosbagFormatException("First record in the file must be a bag header.");
        }
        byte[] data = readRecordDataAsBytes();
        for (byte e : data) {
            if (e != 0x20) {
                throw new InvalidRosbagFormatException("Bag header data should contain only  a sequence of filling 0x20 characters");
            }
        }
    }

    private byte[] readRecordDataAsBytes() throws IOException, UnexpectedEndOfRosbagFileException {
        int bytesCount = readInt();
        byte[] data = new byte[bytesCount];
        if (input.read(data) != bytesCount) {
            throw new UnexpectedEndOfRosbagFileException("End of rosbag file found while reading record data.");
        }
        return data;
    }

    /**
     * Skips the record data. The position in the file will move on the next
     * records header.
     *
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException
     */
    private void skipRecordData() throws IOException, UnexpectedEndOfRosbagFileException {
        int bytesCount = readInt();
        skipBytes(bytesCount);
    }
/**
 * Skips certain number of bytes in the input file.
 * @param bytesCount number of bytes to skip.
 * @throws IOException
 * @throws UnexpectedEndOfRosbagFileException 
 */
    private void skipBytes(int bytesCount) throws IOException, UnexpectedEndOfRosbagFileException {
        if (!readerSupport.skipExactCount(input, bytesCount)) {
            throw new UnexpectedEndOfRosbagFileException("End of rosbag file found while reading record data.");
        }
    }

    private RosTime getTime(Map<String, byte[]> header) throws RequiredFieldMissingRosbagException, InvalidFieldValueRosbagException {
        if (!header.containsKey(RosbagHeaderFieldNames.TIME)) throw new RequiredFieldMissingRosbagException("Each message-data header needs to have 'time' field.");
        byte[] timeBytes = header.get(RosbagHeaderFieldNames.TIME);
        if (timeBytes.length!=8) throw new InvalidFieldValueRosbagException("Message-data header time field's value should be 8 bytes long.");
        int timeSecs = readerSupport.readLittleEndianInt(Arrays.copyOfRange(timeBytes, 0, 4));   
        int timeNanos = readerSupport.readLittleEndianInt(Arrays.copyOfRange(timeBytes, 4, 8));

        return new RosTime(timeNanos, timeSecs);
    }

    /**
     * Parses the content of a chunk.
     * @param chunkHeader
     * @param parser 
     */
    private void parseChunkRecord(Map<String, byte[]> chunkHeader, RosbagMessageDataParser parser) throws IOException, UnexpectedEndOfRosbagFileException, RequiredFieldMissingRosbagException, InvalidRosbagFormatException, InvalidFieldValueRosbagException {
       // Map<Integer, String> chunkTopics = new HashMap<>();
        int bytesSize = readInt();
        int bytesRead=0;
        ensureFieldExist(chunkHeader,RosbagHeaderFieldNames.COMPRESSION);
        String compression = new String(chunkHeader.get(RosbagHeaderFieldNames.COMPRESSION),StandardCharsets.UTF_8);
        //If required: get bz2 library, get uncompressed size length (size field), wrap InputStream
        if (!compression.equals("none")) throw new UnsupportedOperationException("Chunks with compression not supported yet."); 
        
        while(bytesRead<bytesSize) {
            //bytesRead+=8; // For header size integer and data size integer.
            int headerSize = readInt();
            Map<String, byte[]> header = readHeader(headerSize);
            bytesRead+=headerSize+4; //+4 for the "headerSize"  size
            int opCode = getOpCode(header);
            switch (opCode) {
                case RosOpCodes.CONNECTION:
//                    bytesRead+= parseConnection(header, chunkTopics);
                    bytesRead+= parseConnection(header, topics);
                    break;
                case RosOpCodes.MESSAGE_DATA:
//                    bytesRead+=parseMessageRecord(header, parser, chunkTopics);
                    bytesRead+=parseMessageRecord(header, parser, topics);
                    break;
                case RosOpCodes.INDEX_DATA:
                case RosOpCodes.CHUNK_INFO:
                    //Metadata records are ignored
                    int count = readInt();
                    skipBytes(count);
                    bytesRead+=count+4;
                    break;
                default:
                    throw new InvalidRosbagFormatException("Chunks should contain only connection and message data headers according to specification!");
                    
            }
        }
        if (bytesRead>bytesSize) throw new InvalidRosbagFormatException("Chunk size exceeded it's declared size");
        
    }
/**
 * Obtains the connection id and topic from the header.
 * The content of the record (details about the connection) is ignored.
 * @param header The header of a connection record
 * @param chunkTopics Map of topics to which the pair id->topic will be added.
 * @return number of bytes read
 * @throws UnexpectedEndOfRosbagFileException
 * @throws RequiredFieldMissingRosbagException
 * @throws IOException
 * @throws InvalidFieldValueRosbagException 
 */
    private int parseConnection(Map<String, byte[]> header, Map<Integer, String> chunkTopics) throws UnexpectedEndOfRosbagFileException, RequiredFieldMissingRosbagException, IOException, InvalidFieldValueRosbagException {
        int connectionId = getConnectionId(header);
        String topicName = getConnectionTopic(header);
        chunkTopics.put(connectionId, topicName);
        int connectionData=readInt();
        //Connection details are ignored
        skipBytes(connectionData);
        //The data could be obtained by:
        //Map<String, byte[]> info = readHeader(connectionId);
        return connectionData+4;
    }

    private void ensureFieldExist(Map<String, byte[]> header, String key) throws RequiredFieldMissingRosbagException {
        if (!header.containsKey(key)) {
            throw new RequiredFieldMissingRosbagException("Required field with key "+key+" not found.");
        }
    }
        /**
     * Only wraps the {@link ReaderSupport#tryReadLEInteger(java.io.InputStream, java.lang.Integer) } 
     * to throw the proper exception. 
     * @see ReaderSupport#tryReadLEInteger(java.io.InputStream, java.lang.Integer) 
     * @param s
     * @param result
     * @return
     * @throws IOException
     * @throws UnexpectedEndOfRosbagFileException 
     */
    private boolean tryReadLEInteger(InputStream s, ReaderSupport.IntWrapper result) throws IOException, UnexpectedEndOfRosbagFileException {
        try {
            return readerSupport.tryReadLEInteger(s, result);
        } catch (UnexpectedEndOfFileException ex) {
            throw new UnexpectedEndOfRosbagFileException("End of file found while reading integer.");
        }
    }
    

}
