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
                    if (msgIn.getCmd() == Command.QUERYMSG) {
                        QueryMessage qm = msgIn.getQueryMessage();
                        // Todo: process this message
                    } else if (msgIn.getCmd() == Command.HITMSG) {
                        HitMessage hm = msgIn.getHitMessage();
                        // Todo: process this message
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
