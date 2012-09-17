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
import java.util.HashMap;
import java.util.Map;
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
            assertEquals(true, (Boolean) in.getBody());


            //clean up
            //is.close();
            //os.close();
            sock.close();

        } catch (Exception ex) {
            Logger.getLogger(IndexServerTestCase.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
    }

    @Test
    public void testRegistry() {
        try {
            //#
            //First Establish connection
            Socket sock = new Socket("localhost", 5700);
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            //#
            //Second make input files
            // 1. peerId
            // 2. Message (REG, 
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("peerId", "null");
            String[] files = {"file1.txt", "file2.JPEG", "file3.xml"};
            //parameters.put("port", port);
            parameters.put("files", files);
            //#
            //Third send output
            P2PProtocol protocol = new P2PProtocol();
            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.REG, parameters);
            protocol.preparedOutput(os, messageOut);
            sock.shutdownOutput();
            //#
            //Forth assert return value
            P2PProtocol.Message messageIn = protocol.processInput(is);
            assertEquals(messageIn.getCmd(), P2PProtocol.Command.OK);
            sock.close();
        } catch (Exception ex) {
            Logger.getLogger(IndexServerTestCase.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

    }

    @Test
    public void testSearch() {
        //#
    }

    @Test
    public void testLookup() {
    }

    @Test
    public void testUnknownCommand() {
    }
}
