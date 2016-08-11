/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rosbagreader;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import rosbagreader.exceptions.UnexpectedEndOfRosbagFileException;

/**
 *
 * @author Tomas Prochazka
 */
public class RosbagReaderTest {

    public RosbagReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testParseBag() throws Exception {
        FileInputStream file = new FileInputStream("ArdroneFlight.bag");
        RosbagReader rosbag = new RosbagReader(file);
        rosbag.parseBag(new RosbagMessageDataParser() {

            @Override
            public void parseMessageData(RosMessageData message) throws IOException, UnexpectedEndOfRosbagFileException {

                if (message.getTopic().equals("/ardrone/navdata")) {
                    int readLittleEndianInt = 0;
                    long m = message.readUnsignedInt();
                    RosTime t = message.readTime();
                    String s = message.readString();
                    float battery = message.readFloat();
                }
            }
        });

    }

    @Test
    public void testParseBag2() throws Exception {
        FileInputStream file = new FileInputStream("allNavdataStraightFlight.bag");
        RosbagReader rosbag = new RosbagReader(file);
        rosbag.parseBag(new RosbagMessageDataParser() {

            @Override
            public void parseMessageData(RosMessageData message) throws IOException, UnexpectedEndOfRosbagFileException {

                int readLittleEndianInt = 0;
                //long m = message.readUnsignedInt();
                //RosTime t = message.readTime();
                //String s = message.readString();
                //float battery = message.readFloat();

            }
        });

    }

    @Test
    public void testParseImage() throws Exception {
        FileInputStream file = new FileInputStream("allNavdataStraightFlight.bag");
        RosbagReader rosbag = new RosbagReader(file);
        rosbag.parseBag(new RosbagMessageDataParser() {

            int c = 1;

            public void parseMessageData(RosMessageData message) throws UnexpectedEndOfRosbagFileException, IOException {
                if (message.getTopic().equals("/ardrone/image_raw/compressed")) {

                    if (true) {
                        return;
                    }
                    RosStandardMessageHeader readMessageHeader = message.readMessageHeader();
                    String readString = message.readString();
                    byte[] messageBytes = message.readBytes(message.getBytesLeft());
                    ByteArrayInputStream s = new ByteArrayInputStream(messageBytes);
                    FileOutputStream fos = new FileOutputStream("bytes.jpg");
                    fos.write(messageBytes);
                    fos.close();
                    // BufferedImage i = ImageIO.read(s);
                    Iterator<?> readers = ImageIO.getImageReadersByFormatName("jpg");
                    ImageReader r = (ImageReader) readers.next();
                    r.setInput(ImageIO.createImageInputStream(s), true);
                    Image i = r.read(0);
                    final File fileOutput = new File("c:/ardrone_image.jpg");
                    // ImageIO.write(i, "jpg", fileOutput);
                }
                if (message.getTopic().equals("/ardrone/front/image_raw")) {

                    RosStandardMessageHeader readMessageHeader = message.readMessageHeader();
                    long height = message.readUnsignedInt();
                    long width = message.readUnsignedInt();
                    String compression = message.readString();
                    int isBigEndian = message.readByte();
                    long step_row_length = message.readUnsignedInt();
                    byte[] messageBytes = message.readBytes(message.getBytesLeft());
                    BufferedImage i = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            int pos = y * (int) step_row_length + x * 3;
                            int rgb = messageBytes[pos+2];
                            rgb = (rgb << 8) + messageBytes[pos + 1];
                            rgb = (rgb << 8) + messageBytes[pos ];
                            i.setRGB(x, y, rgb);
                        }
                    }
                    ImageIO.write(i, "png", new File("imgs/Ardrone_image" + (c++) + ".png"));
                }
            }
        });

    }

}
