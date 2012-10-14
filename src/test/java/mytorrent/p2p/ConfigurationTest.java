/*
 * The MIT License
 *
 * Copyright 2012 Bo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package mytorrent.p2p;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Bo
 */
public class ConfigurationTest {

    private static Configuration instance;

    public ConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        try {
            instance = Configuration.load("config.yml");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigurationTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
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

    /**
     * Test of load method, of class Configuration.
     */
    @Test
    public void testLoad() {
        System.out.println("load");
        assertNotNull(instance);
    }

    /**
     * Test of getHostAddress method, of class Configuration.
     */
    @Test
    public void testGetHostAddress() {
        System.out.println("getHostAddress");
        PeerAddress expResult = new PeerAddress(101L, "192.168.1.101", 5700, 5711);
        PeerAddress result = instance.getHostAddress();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNeighbors method, of class Configuration.
     */
    @Test
    public void testGetNeighbors() {
        System.out.println("getNeighbors");
        PeerAddress[] expResult = {new PeerAddress(102L, "192.168.1.102", 5700, 5711), new PeerAddress(103, "192.168.1.103", 5700, 5711)};
        PeerAddress[] result = instance.getNeighbors();
        assertArrayEquals(expResult, result);
    }
}
