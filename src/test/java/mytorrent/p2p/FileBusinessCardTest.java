/*
 * The MIT License
 *
 * Copyright 2012 bfeng.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import mytorrent.p2p.FileBusinessCard;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author bfeng
 */
public class FileBusinessCardTest {

    private InputStream in;
    private OutputStream out;
    private P2PProtocol protocol;
    public ConcurrentHashMap<String, FileBusinessCard> Push_file_map;
    public ArrayDeque<String> Push_broadcast_external;

    public FileBusinessCardTest() {
    }

    @Before
    public void setUp() {
        try {
            //here I'm using a pipeline to mimic the network.
            in = new PipedInputStream();
            out = new PipedOutputStream((PipedInputStream) in);

            protocol = new P2PProtocol();
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void test_CreateFBC() {
        FileBusinessCard newCard = new FileBusinessCard("filename", 101, "localhost", 1, 2);

        assertEquals(newCard.get_filename(), "filename");
        assertEquals(newCard.get_peerHost(), "localhost");

        newCard.setTTR(10);
        assertEquals(newCard.get_TTRthreshold(), 10);
    }

    @Test
    public void test_FBCinDeque() {
        FileBusinessCard newCard = new FileBusinessCard("filename", 101, "localhost", 1, 2);
        Push_file_map.put("filename", newCard);
        Push_broadcast_external.offer("filename");
        String pollresult = Push_broadcast_external.poll();
        assertEquals(pollresult, "filename");
        assertEquals(Push_broadcast_external.isEmpty(), true);
        assertEquals(Push_file_map.get("filename"), newCard);
        
    }

    @Test
    public void test_ReplaceFBC() {
        FileBusinessCard newCard = new FileBusinessCard("filename", 101, "localhost", 1, 2);
        newCard.set_approach(FileBusinessCard.Approach.NULL);
        Push_file_map.clear();
        Push_file_map.put("filename", newCard);
        newCard.set_state(FileBusinessCard.State.VALID);
        Push_file_map.replace("filename", newCard);
        FileBusinessCard theCard = Push_file_map.get("filename");
        
        assertEquals(theCard, newCard);
        
    }
}
