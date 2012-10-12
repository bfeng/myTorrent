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
import mytorrent.p2p.P2PProtocol.Command;
import mytorrent.p2p.P2PProtocol.HitMessage;
import mytorrent.p2p.P2PProtocol.Message;
import mytorrent.p2p.P2PProtocol.QueryMessage;
import mytorrent.p2p.P2PProtocol.Result;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author bfeng
 */
public class P2PTest {

    private InputStream in;
    private OutputStream out;
    private P2PProtocol protocol;

    public P2PTest() {
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
    public void test_Transfer_QueryMessage() {
        final long peerId = 101;
        final long messageId = 100;
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        QueryMessage qm = protocol.new QueryMessage(peerId, messageId, 3);
                        Message msgOut = protocol.new Message(qm);
                        protocol.processOutput(out, msgOut);
                    }
                }).start();
        Message msgIn = protocol.processInput(in);
        assertNotNull(msgIn);
        assertEquals(Command.QUERYMSG, msgIn.getCmd());
        QueryMessage gotQuery = msgIn.getQueryMessage();
        assertNotNull(gotQuery);
        assertEquals(peerId, gotQuery.getPeerID());
        assertEquals(messageId, gotQuery.getMessageID());
    }

    @Test
    public void test_Transfer_HitMessage() {
        final long peerId = 101;
        final long messageId = 100;
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // suppose I got a query message
                        QueryMessage qm = protocol.new QueryMessage(peerId, messageId, 3);
                        HitMessage hm = protocol.new HitMessage(qm);

                        // suppose I can't find the requested file.
                        hm.miss();

                        Message msgOut = protocol.new Message(hm);
                        protocol.processOutput(out, msgOut);
                    }
                }).start();
        Message msgIn = protocol.processInput(in);
        assertNotNull(msgIn);
        assertEquals(msgIn.getCmd(), Command.HITMSG);
        HitMessage gotHit = msgIn.getHitMessage();
        assertNotNull(gotHit);
        assertEquals(peerId, gotHit.getPeerID());
        assertEquals(messageId, gotHit.getMessageID());

        assertEquals(Result.MISS, gotHit.getResult());
    }

    @Test
    public void test_Forward_QueryMessage() {
        final long peerId = 101;
        final long messageId = 100;
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        QueryMessage qm = protocol.new QueryMessage(peerId, messageId, 3);
                        Message msgOut = protocol.new Message(qm);
                        protocol.processOutput(out, msgOut);
                    }
                }).start();

        try {
            final InputStream inN = new PipedInputStream();
            final OutputStream outN = new PipedOutputStream((PipedInputStream) inN);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msgIn = protocol.processInput(in);
                    assertEquals(Command.QUERYMSG, msgIn.getCmd());
                    QueryMessage gotQuery = msgIn.getQueryMessage();

                    // continue to forward
                    gotQuery.addPath(102);
                    Message msgOut = protocol.new Message(gotQuery);
                    protocol.processOutput(outN, msgOut);
                }
            }).start();

            Message msgIn = protocol.processInput(inN);
            assertNotNull(msgIn);
            assertEquals(Command.QUERYMSG, msgIn.getCmd());
            QueryMessage gotQuery = msgIn.getQueryMessage();
            assertNotNull(gotQuery);
            assertEquals(peerId, gotQuery.getPeerID());
            assertEquals(messageId, gotQuery.getMessageID());
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void test_Reply_HitMessage() {
        try {
            final long peerId = 101;
            final long messageId = 100;
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            QueryMessage qm = protocol.new QueryMessage(peerId, messageId, 3);
                            Message msgOut = protocol.new Message(qm);
                            protocol.processOutput(out, msgOut);
                        }
                    }).start();

            final InputStream inN = new PipedInputStream();
            final OutputStream outN = new PipedOutputStream((PipedInputStream) inN);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msgIn = protocol.processInput(in);
                    assertEquals(Command.QUERYMSG, msgIn.getCmd());
                    QueryMessage gotQuery = msgIn.getQueryMessage();

                    // continue to forward
                    gotQuery.addPath(102);
                    Message msgOut = protocol.new Message(gotQuery);
                    protocol.processOutput(outN, msgOut);
                }
            }).start();


            final InputStream inM = new PipedInputStream();
            final OutputStream outM = new PipedOutputStream((PipedInputStream) inM);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msgIn = protocol.processInput(inN);
                    assertNotNull(msgIn);
                    assertEquals(Command.QUERYMSG, msgIn.getCmd());
                    QueryMessage gotQuery = msgIn.getQueryMessage();
                    assertNotNull(gotQuery);
                    assertEquals(peerId, gotQuery.getPeerID());
                    assertEquals(messageId, gotQuery.getMessageID());


                    // reply a hit
                    HitMessage hm = protocol.new HitMessage(gotQuery);
                    hm.hit("192.168.1.1", 5711);
                    assertEquals(102, hm.nextPath().longValue());
                    Message msgOut = protocol.new Message(hm);
                    protocol.processOutput(outM, msgOut);
                }
            }).start();

            Message msgIn = protocol.processInput(inM);
            assertNotNull(msgIn);
            assertEquals(Command.HITMSG, msgIn.getCmd());
            HitMessage gotHit = msgIn.getHitMessage();
            assertNotNull(gotHit);
            assertEquals(peerId, gotHit.getPeerID());
            assertEquals(messageId, gotHit.getMessageID());
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }
}
