package mytorrent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author bfeng
 */
public class IndexServerTestCase {

    public IndexServerTestCase() {
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
    public void testPing() {
        for (int i = 0; i < 100; i++) {
            try {
                Socket sock = new Socket("localhost", 5700);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                out.println("ping");
                out.flush();

                assertEquals("ping", in.readLine());

                in.close();
                out.close();
                sock.close();

            } catch (Exception ex) {
                Logger.getLogger(IndexServerTestCase.class.getName()).log(Level.SEVERE, null, ex);
                fail(ex.getMessage());
            }
        }
    }
}
