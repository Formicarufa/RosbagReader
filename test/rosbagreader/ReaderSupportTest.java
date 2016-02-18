/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rosbagreader;

import support.ReaderSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rosbagreader.exceptions.UnexpectedEndOfFileException;

/**
 *
 * @author Tomas Prochazka
 */
public class ReaderSupportTest {
    
    public ReaderSupportTest() {
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
	public void littleEndianIntTest1() throws IOException, UnexpectedEndOfFileException {
		ReaderSupport r = new ReaderSupport();
		ByteArrayInputStream i = new ByteArrayInputStream(new byte[] { 1, 0, 0, 0 });
		int res = r.readLittleEndianInt(i);
		assertEquals(1, res);
	}

	@Test
	public void littleEndianIntTestMaxValue() throws IOException, UnexpectedEndOfFileException {
		ReaderSupport r = new ReaderSupport();
		byte[] bytes = new byte[4];
		ByteBuffer b = ByteBuffer.wrap(bytes);
		b.order(ByteOrder.LITTLE_ENDIAN);
		b.putInt(Integer.MAX_VALUE); //bytes: -1,-1,-1,127
		ByteArrayInputStream i = new ByteArrayInputStream(bytes);
		int res = r.readLittleEndianInt(i);
		assertEquals(Integer.MAX_VALUE, res);
	}
	@Test
	public void littleEndianLongTest1() throws IOException, UnexpectedEndOfFileException{
		ReaderSupport r = new ReaderSupport();
		ByteArrayInputStream i = new ByteArrayInputStream(new byte[] { 1, 0, 0, 0,0,0,0,0 });
		long res = r.readLittleEndianLong(i);
		assertEquals(1, res);
	}
	@Test
	public void littleEndianLongTestMaxValue() throws IOException, UnexpectedEndOfFileException{
		ReaderSupport r = new ReaderSupport();
		ByteArrayInputStream i = new ByteArrayInputStream(new byte[] { -1, -1, -1, -1,-1,-1,-1,127 });
		long res = r.readLittleEndianLong(i);
		assertEquals(Long.MAX_VALUE, res);
	}


    
}
