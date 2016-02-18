/**
 *
 */
package support;

import rosbagreader.exceptions.UnexpectedEndOfFileException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * @author Tomas Prochazka Java built-in reader supports reading of int / long
 * only in the Big Endian byte order, this class provides methods to read
 * little-endian ints and longs.
 */
public class ReaderSupport {

    /**
     * Buffer shared by all the functions.
     */
    private final ByteBuffer buffer = ByteBuffer.allocate(8);

    /**
     * Reads 4 bytes from the input stream and interprets them as an integer,
     * using little-endian byte order
     *
     * @param input Input stream to read from
     * @return 4 bytes from the input stream interpreted as an integer.
     * @throws java.io.IOException
     * @throws rosbagreader.exceptions.UnexpectedEndOfFileException
     */
    public int readLittleEndianInt(InputStream input) throws IOException, UnexpectedEndOfFileException {

        byte[] bytes = readBytesFromInputStream(4, input);

        return readLittleEndianInt(bytes);
    }

    private byte[] readBytesFromInputStream(int bytesCount, InputStream input) throws UnexpectedEndOfFileException, IOException {
        byte[] bytes = new byte[bytesCount];
        if (bytesCount != input.read(bytes, 0, bytesCount)) {
            throw new UnexpectedEndOfFileException();
        }
        return bytes;
    }

    /**
     * Interprets the bytes as an integer, using little-endian byte order
     *
     * @param bytes 4 bytes
     * @return a 4 bytes as an integer in little-endian order
     */
    public int readLittleEndianInt(byte[] bytes) {
        resetBuffer(bytes);
        return buffer.getInt();
    }

    /**
     * Reads 8 bytes from the input stream and interprets them as a long
     * integer, using little-endian byte order
     *
     * @param input Input stream to read from
     * @return 8 bytes from the input stream interpreted as a long.
     * @throws java.io.IOException
     * @throws rosbagreader.exceptions.UnexpectedEndOfFileException
     */
    public long readLittleEndianLong(InputStream input) throws IOException, UnexpectedEndOfFileException {
        byte[] bytes = readBytesFromInputStream(8, input);
        return readLittleEndianLong(bytes);
    }

    /**
     * Converts bytes to long
     *
     * @param bytes array containing 8 bytes
     * @return 8 bytes as a long in little-endian order
     */
    public long readLittleEndianLong(byte[] bytes) {
        resetBuffer(bytes);
        return buffer.getLong();
    }

    /**
     * Returns the interpretation of the byte as if it was unsigned
     *
     * @param b byte -128 ... 127
     * @return int 0 ... 255
     */
    public int byteAsUnsigned(byte b) {
        return b & 0xFF;
    }

    /**
     * Interprets the byte sequence as a double, using little-endian byte order.
     *
     * @param bytes 8 bytes
     * @return 8 bytes interpreted as a double in little-endian order
     */
    public double readLittleEndianDouble(byte[] bytes) {
        resetBuffer(bytes);
        return buffer.getDouble();
    }

        /**
     * Reads 8 bytes from the input stream and interprets them as a double,
     * using little-endian byte order
     *
     * @param stream Input stream to read from
     * @return 8 bytes from the input stream interpreted as a double.
     * @throws rosbagreader.exceptions.UnexpectedEndOfFileException
     * @throws java.io.IOException
     */
    public double readLittleEndianDouble(InputStream stream) throws UnexpectedEndOfFileException, IOException {
        byte[] bytes = readBytesFromInputStream(8, stream);
        return readLittleEndianDouble(bytes);
    }

    /**
     * Interprets the bytes as a float, using little-endian byte order
     *
     * @param bytes 4 bytes
     * @return a 4 bytes as a float in little-endian order
     */
    public float readLittleEndianFloat(byte[] bytes) {
        resetBuffer(bytes);
        return buffer.getFloat();
    }

          /**
     * Reads 4 bytes from the input stream and interprets them as a float,
     * using little-endian byte order
     *
     * @param stream Input stream to read from
     * @return 4 bytes from the input stream interpreted as a float.
     * @throws rosbagreader.exceptions.UnexpectedEndOfFileException
     * @throws java.io.IOException
     */
    public float readLittleEndianFloat(InputStream stream) throws UnexpectedEndOfFileException, IOException {
        byte[] bytes = readBytesFromInputStream(4, stream);
        return readLittleEndianFloat(bytes);
    }
    /**
     * Resets the buffer and sets it's bytes to the given value.
     *
     * @param bytes
     */
    private void resetBuffer(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes);
        buffer.flip();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }
    /**
     * Converts the byte array to string.
     * String should be encoded as UTF-8.
     * @param bytes
     * @return 
     */
    public String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
    /**
     * Skips the given number of bytes of the input stream. 
     * Assuming there is enough bytes to skip in the file, skips exactly the given number of bytes.
     * The method {@link InputStream#skip(long) } skips only the data
     * which are currently available. For example, with BufferedInputStream
     * the skip method skips only the data in the buffer.
     * Because we want to be sure we skip exactly the given number of bytes,
     * this alternative (blocking) implementation is used.
     * @param stream input stream from which the data are taken
     * @param count number of bytes to skip
     * @return true if the data were skipped, false if the end of file has been reached.
     * @throws IOException 
     */
    public boolean skipExactCount(InputStream stream, int count) throws IOException {
        int i = 0;
        int res = 0;
        while (i<count&& ((res=stream.read())!=-1)) {
            i++;
        }
        return res!=-1;
    }
    /**
     * Tries to read little endian integer from the input stream. It is expected
     * that either it will be possible to read whole integer or there will be 
     * no data at all in the stream. 
     * @param input input stream to read the integer from.
     * @param result Integer read from the stream is returned through this variable.
     * @return true if the integer was read successfully, false if the stream was empty
     * @throws IOException 
     * @throws rosbagreader.exceptions.UnexpectedEndOfFileException  Thrown only in case that part of the bytes of the integer have been read successfully.
     */
    public boolean tryReadLEInteger(InputStream input, IntWrapper result) throws IOException, UnexpectedEndOfFileException {
        byte[] bytes = new byte[4];
        int i;
        i=input.read();
        if (i==-1) return false;
        bytes[0]=(byte)i;
        i=input.read();
        bytes[1]=(byte)i;
        if (i==-1) throw new UnexpectedEndOfFileException();
        i=input.read();
        bytes[2]=(byte)i;
        if (i==-1) throw new UnexpectedEndOfFileException();
        i=input.read();
        if (i==-1) throw new UnexpectedEndOfFileException();
        bytes[3]=(byte)i;
        result.i = readLittleEndianInt(bytes);
        return true;
    }
    public static class IntWrapper {
        public int i;
    }

}
