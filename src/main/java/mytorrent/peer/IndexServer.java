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
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.FileHash;
import mytorrent.p2p.P2PProtocol;
import mytorrent.p2p.P2PProtocol.Command;
import mytorrent.p2p.P2PProtocol.HitMessage;
import mytorrent.p2p.P2PProtocol.Message;
import mytorrent.p2p.P2PProtocol.QueryMessage;
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
    private static FileHash fileHash = new FileHash();
    private static FileHash remoteFileHash = new FileHash();
    private static PeerHash peerHash = new PeerHash();

    public IndexServer(PeerAddress host, PeerAddress[] neighbors) {
        this.host = host;
        this.neighbors = neighbors;
    }

    public void updateFileHash(String[] files) {
        //#1
        //Clear current filehash
        fileHash.emptyAll();
        //#2
        //Get host peerId for filehash struct
        long hostpeerId = host.getPeerID();
        //#3
        //Reconstruct filehash
        if (files != null && files.length > 0) {
            for (String item : files) {
                fileHash.addEntry(fileHash.new Entry(hostpeerId, item));
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

    public String obtain_host_for(Long peerID) {
        return peerHash.findIndexServerAddress(peerID);
    }

    public int obtain_FSport_for(Long peerID) {
        return peerHash.findFileServerPort(peerID);
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

        private boolean checkMessage(P2PProtocol.Message in) {
            if (in != null && in.getCmd() != null) {
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            Message msgOut = null;
            try {
                Message msgIn = protocol.processInput(socket.getInputStream());
                if (checkMessage(msgIn)) {
                    //MAIN BRANCH
                    if (msgIn.getCmd() == Command.QUERYMSG) {
                        QueryMessage qm = msgIn.getQueryMessage();

                        // Todo: process this message
                        // if this is my message, then ignore
                        // else search that file
                        // if I can find the file
                        // return a hitmessage with result
                        // else return a hitmessage with miss
                        // finally, add my id to path 
                        // and forward the message to all of my neighbors if TTL > 0

                        if ( //#
                                //I wont do it again if I started the Query       
                                (host.getPeerID() != (int) qm.getPeerID())
                                //#
                                //It won't pass me again if this message passed thru me before
                                && (!qm.searchPath(host.getPeerID()))) {
                            //#
                            //Frisk myself:
                            //search local files
                            FileHash.Entry localResults[] = fileHash.search(qm.getFilename());
                            //execute Query or HitQuery while preparing message
                            if (localResults.length < 1) {
                                //#miss
                                //prepare query and hit query
                                //## Query
                                if (qm.isLive()) {
                                    //prepare Query msg
                                    P2PProtocol.QueryMessage forwardQuery = qm;
                                    //Decrement TTL
                                    forwardQuery.decrementTTL();
                                    //Add Path
                                    forwardQuery.addPath(host.getPeerID());
                                    //generate msg to send out
                                    P2PProtocol.Message forwardQueryMsgOut = protocol.new Message(forwardQuery);
                                    //send msg out to neighbour
                                    String aNeighborHost = null;
                                    int aNeighborPort = -1;
                                    Socket aNeighborSocket = null;
                                    for (PeerAddress aNeighbor : neighbors) {
                                        aNeighborHost = aNeighbor.getPeerHost();
                                        aNeighborPort = aNeighbor.getIndexServerPort();
                                        aNeighborSocket = new Socket(aNeighborHost, aNeighborPort);
                                        protocol.processOutput(aNeighborSocket.getOutputStream(), forwardQueryMsgOut);
                                        aNeighborSocket.shutdownOutput();
                                    }
                                }
                                //## HitQuery-miss
                                //prepare HitQuery msg
                                P2PProtocol.HitMessage generateHitQuery = protocol.new HitMessage(qm);
                                //ensure it is a miss
                                generateHitQuery.miss();
                                //### send to pop path or owner
                                int HitQueryNextNode = -1;
                                //owner
                                if (generateHitQuery.size() == 0) {
                                    HitQueryNextNode = (int) generateHitQuery.getPeerID();
                                } else {
                                    //pop path from deque
                                    HitQueryNextNode = generateHitQuery.nextPath().intValue();
                                }
                                //generate msg to send out after pop or identified empty deque in the last step ONLY
                                P2PProtocol.Message generateHitQueryMsgOut = protocol.new Message(generateHitQuery);
                                //look up path from neighbour
                                String theNeighbourHost = null;
                                int theNeighbourPort = -1;
                                for (PeerAddress neighbours : neighbors) {
                                    if (neighbours.getPeerID() == HitQueryNextNode) {
                                        theNeighbourHost = neighbours.getPeerHost();
                                        theNeighbourPort = neighbours.getIndexServerPort();
                                        break;
                                    }
                                }
                                //Exceptions:
                                if (theNeighbourHost == null || theNeighbourPort == -1) {
                                    System.out.println("ERROR sending Query. Msg doesn't match network configuration");
                                }

                                //send msg out to THE neighbour
                                Socket theNeighbourSocket = new Socket(theNeighbourHost, theNeighbourPort);
                                protocol.processOutput(theNeighbourSocket.getOutputStream(), generateHitQueryMsgOut);
                                theNeighbourSocket.shutdownOutput();

                            } else if (localResults.length >= 1) {
                                //#hit
                                //DO NOT forward any Query message
                                //ONLY generate HitQuery Message
                                P2PProtocol.HitMessage generateHitQuery = protocol.new HitMessage(qm);
                                //ensure it is a hit
                                generateHitQuery.hit((long) host.getPeerID(), host.getPeerHost(), host.getFileServerPort(), host.getIndexServerPort());
                                //Send it back via reverse-path, either owner or pop path
                                int HitQueryNextNode = -1;
                                //owner
                                if (generateHitQuery.size() == 0) {
                                    HitQueryNextNode = (int) generateHitQuery.getPeerID();
                                } else {
                                    //pop path from deque
                                    HitQueryNextNode = generateHitQuery.nextPath().intValue();
                                }
                                //generate msg to send out after pop or identified empty deque in the last step ONLY
                                P2PProtocol.Message generateHitQueryMsgOut = protocol.new Message(generateHitQuery);
                                //look up path from neighbour
                                String theNeighbourHost = null;
                                int theNeighbourPort = -1;
                                for (PeerAddress neighbours : neighbors) {
                                    if (neighbours.getPeerID() == HitQueryNextNode) {
                                        theNeighbourHost = neighbours.getPeerHost();
                                        theNeighbourPort = neighbours.getIndexServerPort();
                                        break;
                                    }
                                }
                                //Exceptions:
                                if (theNeighbourHost == null || theNeighbourPort == -1) {
                                    System.out.println("ERROR sending Query. Msg doesn't match network configuration");
                                }
                                //send msg out to THE neighbour
                                Socket theNeighbourSocket = new Socket(theNeighbourHost, theNeighbourPort);
                                protocol.processOutput(theNeighbourSocket.getOutputStream(), generateHitQueryMsgOut);
                                theNeighbourSocket.shutdownOutput();
                            }
                        }

                        //MAIN BRANCH
                        //revision: add a machenism for updating remoteFileHash
                    } else if (msgIn.getCmd() == Command.HITMSG) {
                        HitMessage hm = msgIn.getHitMessage();

                        // Todo: process this message
                        // if this is my message, then check the result
                        // else pull out my id from the path and send it to the next one

                        //# identify the ownership of the HitMsg
                        //prepare HitQuery msg
                        P2PProtocol.HitMessage generateHitQuery = hm;
                        int HitQueryNextNode = -1;
                        if ((int) hm.getPeerID() != host.getPeerID()) {
                            //I didn't started it
                            HitQueryNextNode = generateHitQuery.nextPath().intValue();

                            //generate msg to send out after pop or identified empty deque in the last step ONLY
                            P2PProtocol.Message generateHitQueryMsgOut = protocol.new Message(generateHitQuery);
                            //look up path from neighbour
                            String theNeighbourHost = null;
                            int theNeighbourPort = -1;
                            for (PeerAddress neighbours : neighbors) {
                                if (neighbours.getPeerID() == HitQueryNextNode) {
                                    theNeighbourHost = neighbours.getPeerHost();
                                    theNeighbourPort = neighbours.getIndexServerPort();
                                    break;
                                }
                            }
                            //Exceptions:
                            if (theNeighbourHost == null || theNeighbourPort == -1) {
                                System.out.println("ERROR sending Query. Msg doesn't match network configuration");
                            }
                            //send msg out to THE neighbour
                            Socket theNeighbourSocket = new Socket(theNeighbourHost, theNeighbourPort);
                            protocol.processOutput(theNeighbourSocket.getOutputStream(), generateHitQueryMsgOut);
                            theNeighbourSocket.shutdownOutput();
                        } else {
                            //I started it
                            //Ask if update is still allowed

                            //update remotefilehash
                            FileHash.Entry newEntry = remoteFileHash.new Entry(hm.getPeerID(), hm.getFilename());
                            remoteFileHash.addEntry(newEntry);
                            //update peerHash
                            PeerAddress toAddPeerHash = new PeerAddress((int) hm.getHitPeerID(), hm.getHitPeerHost(), hm.getHitPeerISPort(), hm.getHitPeerFSPort());
                            peerHash.addValue(hm.getHitPeerID(), toAddPeerHash);
                        }
                    }
                }
                if (msgOut == null) {
                    msgOut = protocol.new Message(Command.ERR);
                }
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                msgOut = protocol.new Message(Command.ERR);
            } finally {
                try {
                    protocol.preparedOutput(socket.getOutputStream(), msgOut);
                    socket.shutdownOutput();
                } catch (IOException ex) {
                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
