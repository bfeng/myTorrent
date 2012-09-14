package mytorrent;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Bo Feng
 * @version 1.0
 */
public class PeerTestCase {

    private static Peer peer1;
    private static Peer peer2;

    public PeerTestCase() {
    }

    @BeforeClass
    public static void setUpClass() {
        peer1 = new Peer(5711);
        peer2 = new Peer(5712);

        peer1.startup();
        peer2.startup();
    }

    @AfterClass
    public static void tearDownClass() {
        peer1.exit();
        peer2.exit();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPeerServerStatus() {
        assertTrue(peer1.ping());
        assertTrue(peer2.ping());
    }

    @Test
    public void testDownload() {
        assertNotNull(peer1.obtain("sample.txt"));
    }
    
    @Test
    public void testRegister() {
        long result = peer1.registry(-1, null);
        assertEquals(result, -1);
    }
}
