package mytorrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.P2PProtocol;
import mytorrent.p2p.P2PProtocol.Command;
import mytorrent.p2p.P2PProtocol.Message;
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
//        try {
//            Socket sock = new Socket("localhost", 5700);
//            sock.close();
//        } catch (UnknownHostException ex) {
//            Logger.getLogger(IndexServerTestCase.class.getName()).log(Level.SEVERE, null, ex);
//            fail("To make a test, please start up IndexServer at localhost.");
//        } catch (IOException ex) {
//            Logger.getLogger(IndexServerTestCase.class.getName()).log(Level.SEVERE, null, ex);
//            fail("Cannot find listening IndexServer! Please check the server port number.");
//        }
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
        try {
            Socket sock = new Socket("localhost", 5700);
            //PrintWriter out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
            //BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            //Send Out Put
            boolean ping = true;
            P2PProtocol pp = new P2PProtocol();
            P2PProtocol.Message out = pp.new Message(P2PProtocol.Command.PIG, ping);
            pp.preparedOutput(os, out);
            sock.shutdownOutput();
            
            //Wait for ping-OK back
            P2PProtocol.Message in = pp.processInput(is);
            
            
            //out.println("ping");
            //out.flush();
            assertEquals(P2PProtocol.Command.OK, in.getCmd());
            assertEquals(true, (Boolean)in.getBody());
            
            
            //clean up
            //is.close();
            //os.close();
            sock.close();

        } catch (Exception ex) {
            Logger.getLogger(IndexServerTestCase.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }
}
