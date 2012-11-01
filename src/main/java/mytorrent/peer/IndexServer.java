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
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.FileBusinessCard;
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
    public final VersionMonitor versionMonitor;

    public IndexServer(PeerAddress host, PeerAddress[] neighbors) {
        this.host = host;
        this.neighbors = neighbors;
        this.versionMonitor = new VersionMonitor(this.host);
        this.versionMonitor.start();
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

                        Logger.getLogger(IndexServer.class.getName()).log(Level.FINE, "\nThe hit message should follow the reversed path:\nPath: {0}", hitQuery.debugPath());

                        PeerAddress pa = this.findANeighbor(hitQuery.nextPath());
                        this.send2Peer(msgOut, pa);

                        if (qm.isLive()) {
                            //Decrement TTL
                            qm.decrementTTL();
                            //Add Path
                            qm.addPath(host.getPeerID());
                            //generate msg to send out
                            P2PProtocol.Message forwardQueryMsgOut = protocol.new Message(qm);
                            Logger.getLogger(IndexServer.class.getName()).log(Level.FINE, "\nThe query message should contain the full path:\nPath: {0}", qm.debugPath());
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
                } else if (msgIn.getCmd() == Command.INVALIDATE) {
                    //#-1 check local file for invalidate
                    FileBusinessCard newCard = msgIn.getFileBusinessCard();
                    String filename = newCard.get_filename();
                    if (versionMonitor.p2p_file_map.containsKey(filename) && newCard.get_versionNumber() > versionMonitor.p2p_file_map.get(filename).get_versionNumber()) {
                        versionMonitor.justInvalidate(filename);
                    }
                    //#-2 propagate invalidate if necessary
                    QueryMessage qm = msgIn.getQueryMessage();
                    if (qm.isLive()) {
                        //Decrement TTL
                        qm.decrementTTL();
                        //Add Path
                        qm.addPath(host.getPeerID());
                        //generate msg to send out
                        P2PProtocol.Message forwardINVALIDATEMsgOut = protocol.new Message(newCard);
                        forwardINVALIDATEMsgOut.setQueryMessage(qm);
                        forwardINVALIDATEMsgOut.setCmd(Command.INVALIDATE);

                        Logger.getLogger(IndexServer.class.getName()).log(Level.FINE, "\nThe query message should contain the full path:\nPath: {0}", qm.debugPath());
                        //send msg out to neighbour
                        for (PeerAddress n : neighbors) {
                            try {
                                Socket client = new Socket(n.getPeerHost(), n.getIndexServerPort());
                                protocol.preparedOutput(client.getOutputStream(), forwardINVALIDATEMsgOut);
                                client.shutdownOutput();
                            } catch (UnknownHostException ex) {
                                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                } else if (msgIn.getCmd() == Command.QUERYMSG2) {
                    QueryMessage qm = msgIn.getQueryMessage();

                    // Process this message
                    //  if this is my message, then ignore
                    //  if this message passed me before, then ignore
                    // Search that file
                    //  if I can find the file
                    //  if the file is VALID
                    //      return a hitmessage with result, and a card
                    //      else return a hitmessage with miss without a card
                    // finally, add my id to path 
                    // and forward the message to all of my neighbors if TTL > 0

                    if (host.getPeerID() != qm.getPeerID() && !qm.searchPath(host.getPeerID())) {
                        P2PProtocol.HitMessage hitQuery = protocol.new HitMessage(qm);
                        String filename = qm.getFilename();
                        FileBusinessCard hitCard = null;
                        boolean debug1 = false;
                        boolean p2pHaveFileVALID = false;
                        if(versionMonitor.p2p_file_map.containsKey(filename)){
                            debug1 = true;
                        }
                        if (debug1){
                            if (versionMonitor.p2p_file_map.get(filename).get_state() == FileBusinessCard.State.VALID)
                                p2pHaveFileVALID = false;
                        }
                        if (versionMonitor.Push_broadcast_external.contains(filename) || p2pHaveFileVALID) {
                            hitQuery.hit((long) host.getPeerID(), host.getPeerHost(), host.getFileServerPort(), host.getIndexServerPort());
                            hitCard = versionMonitor.getACard(filename);
                        } else {
                            hitQuery.miss();
                        }
                        Message msgOut = protocol.new Message(hitQuery);
                        msgOut.setFileBusinessCard(hitCard);

                        Logger.getLogger(IndexServer.class.getName()).log(Level.FINE, "\nThe hit message should follow the reversed path:\nPath: {0}", hitQuery.debugPath());

                        PeerAddress pa = this.findANeighbor(hitQuery.nextPath());
                        this.send2Peer(msgOut, pa);

                        if (qm.isLive()) {
                            //Decrement TTL
                            qm.decrementTTL();
                            //Add Path
                            qm.addPath(host.getPeerID());
                            //generate msg to send out
                            P2PProtocol.Message forwardQueryMsgOut = protocol.new Message(qm);
                            forwardQueryMsgOut.setCmd(Command.QUERYMSG2);
                            Logger.getLogger(IndexServer.class.getName()).log(Level.FINE, "\nThe query message should contain the full path:\nPath: {0}", qm.debugPath());
                            //send msg out to neighbour
                            this.send2Neighbors(forwardQueryMsgOut);
                        }
                    }
                } else if (msgIn.getCmd() == Command.CARD) {
                    //send card back to socket
                    String filename = msgIn.getQueryMessage().getFilename();
                    FileBusinessCard returnCard = versionMonitor.getACard(filename);
                    Message msgOut = protocol.new Message(returnCard);
                    msgOut.setCmd(Command.OK);

                    protocol.preparedOutput(this.socket.getOutputStream(), msgOut);
                    this.socket.shutdownOutput();
                }
            } catch (Exception ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    class VersionMonitorCoordinator extends Thread {
        //watch VersionMonitor.Push_broadcast_external for broadcast_invalidate() jobs

        boolean enable;

        public VersionMonitorCoordinator() {
            enable = true;
        }

        @Override
        public void run() {
            while (enable) {
                if (!versionMonitor.Push_broadcast_external.isEmpty()) {
                    String toBroadcast = versionMonitor.Push_broadcast_external.poll();
                    //to broadcast push invalidate to neighbors
                    broadcast_INVALIDATE(toBroadcast, 10);
                    System.out.println(toBroadcast + " need to broadcast !");
                }
                if (!versionMonitor.Pull_poll_external.isEmpty()) {
                    String toPoll = versionMonitor.Pull_poll_external.poll();
                    poll_CARD_update(toPoll);
                    System.out.println("Polling " + toPoll);
                }
            }
        }

        public void pause() {
            enable = false;
        }

        public void broadcast_INVALIDATE(final String filename, final int TTL) {

            new Thread(
                    new Runnable() {

                        @Override
                        public void run() {

                            //# broad cast to all network
                            P2PProtocol protocol = new P2PProtocol();
                            //#-1
                            //generate Query Msg for the use of TTL
                            P2PProtocol.QueryMessage initQueryMsg = protocol.new QueryMessage(host.getPeerID(), 0, TTL);
                            initQueryMsg.setFilename(filename);
                            //generate output MSG
                            P2PProtocol.Message initINVALIDATEMsgOut = protocol.new Message(initQueryMsg);
                            // Make sure it is a INVALIDATE
                            initINVALIDATEMsgOut.setCmd(P2PProtocol.Command.INVALIDATE);
                            //get FileBusinessCard
                            FileBusinessCard newCard = versionMonitor.getACard(filename, versionMonitor.Push_file_map);
                            initINVALIDATEMsgOut.setFileBusinessCard(newCard);
                            //#-2
                            //send out to neighbours
                            for (PeerAddress n : neighbors) {
                                try {
                                    Socket client = new Socket(n.getPeerHost(), n.getIndexServerPort());
                                    protocol.preparedOutput(client.getOutputStream(), initINVALIDATEMsgOut);
                                    client.shutdownOutput();
                                } catch (UnknownHostException ex) {
                                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (IOException ex) {
                                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }).start();
        }

        public void poll_CARD_update(final String filename) {

            new Thread(
                    new Runnable() {

                        @Override
                        public void run() {
                            try {
                                //#-1 Ask for Card
                                P2PProtocol protocol = new P2PProtocol();
                                //generate Query Msg
                                P2PProtocol.QueryMessage initCARDMsg = protocol.new QueryMessage(host.getPeerID(), 0, 0);
                                initCARDMsg.setFilename(filename);
                                //generate output MSG
                                P2PProtocol.Message initCARDMsgOut = protocol.new Message(initCARDMsg);
                                initCARDMsgOut.setCmd(P2PProtocol.Command.CARD);
                                //need the filebusinesscard for address
                                FileBusinessCard filenameCard = versionMonitor.getACard(filename, versionMonitor.p2p_file_map);
                                //send out
                                Socket socketCard = new Socket(filenameCard.get_peerHost(), filenameCard.get_indexServerPort());
                                protocol.preparedOutput(socketCard.getOutputStream(), initCARDMsgOut);
                                socketCard.shutdownOutput();
                                //wait for input
                                P2PProtocol.Message msgIn = protocol.processInput(socketCard.getInputStream());
                                if (msgIn.getCmd() != P2PProtocol.Command.OK) {
                                    System.out.println("Error in asking for FileBusinessCard!");
                                    return;
                                }
                                //process the card
                                FileBusinessCard returnCard = msgIn.getFileBusinessCard();
                                //compare card and process
                                if(returnCard.get_state()!=FileBusinessCard.State.ORIGINAL){
                                    System.out.println("Error in processing "+filename+", returned card is not original!");
                                }
                                if(returnCard.get_versionNumber()>filenameCard.get_versionNumber()){
                                    if(filenameCard.get_state()==FileBusinessCard.State.VALID || filenameCard.get_state()==FileBusinessCard.State.TTR_EXPIRED){
                                        filenameCard.set_state(FileBusinessCard.State.INVALID);
                                    }
                                }
                                

                            } catch (UnknownHostException ex) {
                                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }).start();
        }
    }
}
