package mytorrent;

import com.google.gson.internal.StringMap;
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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.Address;
import mytorrent.p2p.FileHash;
import mytorrent.p2p.FileHash.Entry;
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
        //First, based on testRegistry, we want at least 2 peers has registered indexserver
        try {
            Socket sock = new Socket("localhost", 5700);
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();

            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("peerId", "null");
            String[] files = {"file1.txt", "file2.JPEG", "file4.cc"};

            parameters.put("files", files);

            P2PProtocol protocol = new P2PProtocol();
            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.REG, parameters);
            protocol.preparedOutput(os, messageOut);
            sock.shutdownOutput();
            P2PProtocol.Message messageIn = protocol.processInput(is);
            assertEquals(messageIn.getCmd(), P2PProtocol.Command.OK);
            sock.close();
        } catch (Exception ex) {
            Logger.getLogger(IndexServerTestCase.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
        //#
        //Second, search file1.txt
        //##
        //(1) server shall return OK
        //(2) file1.txt shall have two entries
        try {
            Socket sock = new Socket("localhost", 5700);

            P2PProtocol protocol = new P2PProtocol();
            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.SCH, "file1.txt");
            protocol.preparedOutput(sock.getOutputStream(), messageOut);
            sock.shutdownOutput();

            P2PProtocol.Message messageIn = protocol.processInput(sock.getInputStream());
            assertEquals(messageIn.getCmd(), P2PProtocol.Command.OK);
            //!!
            //The following method will NEVER work:
            //FileHash.Entry[] schResult = (FileHash.Entry[]) ((List<Entry>) messageIn.getBody()).toArray(new FileHash.Entry[((List<Entry>) messageIn.getBody()).size()]);
            //FileHash.Entry[] schResult = (FileHash.Entry[]) ((List) messageIn.getBody()).toArray(new FileHash.Entry[0]);
            //!!
            //'Entry' returned from Message has been transferred, by fromJson, to ArrayList<com.google.gson.internal.StringMap>
            //We have to use these StringMaps to reconstruct our Entry[] if necessary;
            //Gson type cast
            List tempList = (List) messageIn.getBody();
            int tempsize = tempList.size();
            StringMap[] schResult = new StringMap[tempsize];
            tempList.toArray(schResult);



            //FileHash.Entry[] schResult = (FileHash.Entry[]) ((List<Entry>) messageIn.getBody()).toArray(new FileHash.Entry[((List<Entry>) messageIn.getBody()).size()]);
            int correctvalue = 2;
            assertEquals(schResult[0].get("filename"), "file1.txt");
            assertEquals(schResult.length, correctvalue);


        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //#
        //Third, search file4.c
        //##
        //(1) server shall return OK
        //(2) file4.cc shall have only one entry
        try {
            Socket sock = new Socket("localhost", 5700);

            P2PProtocol protocol = new P2PProtocol();
            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.SCH, "file4.cc");
            protocol.preparedOutput(sock.getOutputStream(), messageOut);
            sock.shutdownOutput();

            P2PProtocol.Message messageIn = protocol.processInput(sock.getInputStream());
            assertEquals(messageIn.getCmd(), P2PProtocol.Command.OK);
            //!!
            //The following method will NEVER work:
            //FileHash.Entry[] schResult = (FileHash.Entry[]) ((List<Entry>) messageIn.getBody()).toArray(new FileHash.Entry[((List<Entry>) messageIn.getBody()).size()]);
            //FileHash.Entry[] schResult = (FileHash.Entry[]) ((List) messageIn.getBody()).toArray(new FileHash.Entry[0]);
            //!!
            //'Entry' returned from Message has been transferred, by fromJson, to ArrayList<com.google.gson.internal.StringMap>
            //We have to use these StringMaps to reconstruct our Entry[] if necessary;
            //Gson type cast
            List tempList = (List) messageIn.getBody();
            int tempsize = tempList.size();
            StringMap[] schResult = new StringMap[tempsize];
            tempList.toArray(schResult);

            int correctvalue = 1;
            assertEquals(schResult[0].get("filename"), "file4.cc");
            assertEquals(schResult.length, correctvalue);


        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testLookup() {
        //#
        //First, based on previous @Test {testRegistry, testSearch) the server has registered 10001 and 10002. Lookup 10001 first
        try {
            Socket sock = new Socket("localhost", 5700);

            P2PProtocol protocol = new P2PProtocol();
            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.LOK, 10001);
            protocol.preparedOutput(sock.getOutputStream(), messageOut);
            sock.shutdownOutput();

            P2PProtocol.Message messageIn = protocol.processInput(sock.getInputStream());
            assertEquals(messageIn.getCmd(), P2PProtocol.Command.OK);
            //The Address class is transfered to StringMap
            StringMap lookResult = (StringMap) messageIn.getBody();
            
            assertEquals(lookResult.get("host"),"127.0.0.1");

            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Test
    public void testUnknownCommand() {
        //#
        //Send out OK and expect ERR
        try {
            Socket sock = new Socket("localhost", 5700);

            P2PProtocol protocol = new P2PProtocol();
            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.OK, 12345);
            protocol.preparedOutput(sock.getOutputStream(), messageOut);
            sock.shutdownOutput();

            P2PProtocol.Message messageIn = protocol.processInput(sock.getInputStream());
            assertEquals(messageIn.getCmd(), P2PProtocol.Command.ERR);
            //The Address class is transfered to StringMap
            
            
            assertEquals(messageIn.getBody(),"Command is not supported!");

            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
}
