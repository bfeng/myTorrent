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
package mytorrent.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.FileHash;
import mytorrent.p2p.P2PProtocol;
import mytorrent.p2p.P2PProtocol.Command;
import mytorrent.p2p.P2PProtocol.HitMessage;
import mytorrent.p2p.P2PProtocol.Message;
import mytorrent.p2p.P2PProtocol.QueryMessage;
import mytorrent.p2p.P2PProtocol.Result;
import mytorrent.p2p.PeerAddress;
import mytorrent.p2p.PeerHash;

/**
 *
 * @author Bo Feng
 * @version 2.0
 */
public class IndexServer extends Thread {

    private final PeerAddress[] neighbors;
    private final PeerAddress host;
    private boolean running;
    private ServerSocket listener;
    private static FileHash localFileHash = new FileHash();
    private static FileHash remoteFileHash = new FileHash();
    private static PeerHash peerHash = new PeerHash();

    public IndexServer(PeerAddress host, PeerAddress[] neighbors) {
        this.host = host;
        this.neighbors = neighbors;
    }

    public void updateFileHash(String[] files) {
        //#1
        //Clear current filehash
        localFileHash.emptyAll();
        //#2
        //Get host peerId for filehash struct
        long hostpeerId = host.getPeerID();
        //#3
        //Reconstruct filehash
        if (files != null && files.length > 0) {
            for (String item : files) {
                localFileHash.addEntry(localFileHash.new Entry(hostpeerId, item));
            }
        }
    }

    public synchronized long[] getQueryResult(String filename) {
        long[] peerIDs = null;
        FileHash.Entry[] results = remoteFileHash.search(filename);
        if (results != null && results.length > 0) {
            peerIDs = new long[results.length];
            for (int i = 0; i < results.length; i++) {
                peerIDs[i] = results[i].getPeerId();
            }
        }
        return peerIDs;
    }

    public synchronized FileHash.Entry[] returnRemoteFileHash_for(String filename) {
        return remoteFileHash.search(filename);
    }

    public synchronized void initUpdateRemoteFileHash_for(String filename) {
        //clear all filename entry
        remoteFileHash.removeAllFilename(filename);
    }

    public PeerAddress getPeerAddress(long peerID) {
        return peerHash.get(peerID);
    }

    @Override
    public void run() {
        this.running = true;
        try {
            listener = new ServerSocket(host.getIndexServerPort());
            while (running) {
                try {
                    Socket sock = listener.accept();
                    new Worker(sock).start();
                } catch (SocketException socketException) {
                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, "socket error", socketException);
                    continue;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, "The server is going down", ex);
            this.running = false;
        }

    }

    public void close() {
        this.running = false;
        if (this.listener != null) {
            try {
                this.listener.close();
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, "close error", ex);
            }
        }
    }

    class Worker extends Thread {

        private Socket socket;
        private P2PProtocol protocol;

        public Worker(Socket socket) {
            this.socket = socket;
            this.protocol = new P2PProtocol();
        }

        private boolean ignoreMessage(P2PProtocol.Message in) {
            if (in == null && in.getCmd() == null && in.getCmd() == Command.ERR && in.getCmd() == Command.ING) {
                return true;
            }
            return false;
        }

        private void send2Peer(Message msg, PeerAddress pa) throws IOException {
            Socket client = new Socket(pa.getPeerHost(), pa.getIndexServerPort());
            protocol.preparedOutput(client.getOutputStream(), msg);
            client.shutdownOutput();
        }

        private void send2Neighbors(Message msg) throws IOException {
            for (PeerAddress n : neighbors) {
                this.send2Peer(msg, n);
            }
        }

        private PeerAddress findANeighbor(long neighborID) {
            long[] neiIDs = new long[neighbors.length];
            for (int i = 0; i < neighbors.length; i++) {
                PeerAddress pa = neighbors[i];
                neiIDs[i] = pa.getPeerID();
                if (pa.getPeerID() == neighborID) {
                    return pa;
                }
            }
            throw new RuntimeException(
                    String.format("ERROR sending Query to %d. Known neighbors: [%s]",
                    neighborID, Arrays.toString(neiIDs)));
        }

        @Override
        public void run() {

            try {
                Message msgIn = protocol.processInput(socket.getInputStream());
                if (ignoreMessage(msgIn)) {
                    return;
                } else {
                    Message msgOut = protocol.new Message(Command.ING);
                    protocol.preparedOutput(socket.getOutputStream(), msgOut);
                    socket.shutdownOutput();
                }
                if (msgIn.getCmd() == Command.QUERYMSG) {
                    QueryMessage qm = msgIn.getQueryMessage();

                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, qm.debugPath());

                    // Todo: process this message
                    // if this is my message, then ignore
                    // else search that file
                    // if I can find the file
                    // return a hitmessage with result
                    // else return a hitmessage with miss
                    // finally, add my id to path 
                    // and forward the message to all of my neighbors if TTL > 0

                    if (host.getPeerID() != qm.getPeerID()) {
                        P2PProtocol.HitMessage hitQuery = protocol.new HitMessage(qm);
                        FileHash.Entry localResults[] = localFileHash.search(qm.getFilename());
                        if (localResults.length < 1) {
                            hitQuery.miss();
                        } else if (localResults.length >= 1) {
                            hitQuery.hit((long) host.getPeerID(), host.getPeerHost(), host.getFileServerPort(), host.getIndexServerPort());
                        }
                        Message msgOut = protocol.new Message(hitQuery);
                        
                        Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, "\nThe hit message should follow the reversed path:\nPath: {0}", hitQuery.debugPath());
                        
                        PeerAddress pa = this.findANeighbor(hitQuery.nextPath());
                        this.send2Peer(msgOut, pa);

                        if (qm.isLive()) {
                            //Decrement TTL
                            qm.decrementTTL();
                            //Add Path
                            qm.addPath(host.getPeerID());
                            //generate msg to send out
                            P2PProtocol.Message forwardQueryMsgOut = protocol.new Message(qm);
                            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, qm.debugPath());
                            //send msg out to neighbour
                            this.send2Neighbors(forwardQueryMsgOut);
                        }
                    }

                    //MAIN BRANCH
                } else if (msgIn.getCmd() == Command.HITMSG) {
                    HitMessage hm = msgIn.getHitMessage();

                    // Todo: process this message
                    // if this is my message, then check the result
                    // else pull out my id from the path and send it to the next one

                    if (hm.getPeerID() != host.getPeerID()) {
                        P2PProtocol.Message hitOut = protocol.new Message(hm);
                        PeerAddress nextOne = null;
                        try {
                            nextOne = this.findANeighbor(hm.nextPath());
                        } catch (Exception e) {
                            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, "[HITMSG] This is definitely a bug:\n"
                                    + "Path: " + hm.debugPath(), e);
                        }
                        this.send2Peer(hitOut, nextOne);
                    } else {
                        if (hm.getResult() == Result.HIT) {
                            //update remotefilehash
                            FileHash.Entry newEntry = remoteFileHash.new Entry(hm.getHitPeerID(), hm.getFilename());
                            remoteFileHash.addEntry(newEntry);
                            //update peerHash
                            PeerAddress toAddPeerHash = new PeerAddress(hm.getHitPeerID(), hm.getHitPeerHost(), hm.getHitPeerISPort(), hm.getHitPeerFSPort());
                            peerHash.addValue(hm.getHitPeerID(), toAddPeerHash);
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
