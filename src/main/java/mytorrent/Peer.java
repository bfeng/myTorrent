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
package mytorrent;

import com.google.gson.internal.StringMap;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.Address;
import mytorrent.p2p.FileHash;
import mytorrent.p2p.FileHash.Entry;
import mytorrent.p2p.P2PClient;
import mytorrent.p2p.P2PProtocol;
import mytorrent.p2p.P2PReceiver;
import mytorrent.p2p.P2PSender;
import mytorrent.p2p.P2PTransfer;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Bo Feng
 * @version 1.0
 */
public class Peer implements P2PTransfer, P2PClient {

    private final int port;
    private final FileDownloadServer server;
    private final String indexServerIP;
    private final int indexServerPort;
    private long peerId;
    private Socket socket = null;

    /**
     * This is the constructor of Peer. If there is no IP and port of
     * IndexServer specified, it will use "localhost" and 5700 as the default
     * parameters.
     *
     * @param port
     */
    public Peer(int port) {
        this(port, "localhost", 5700);
        this.peerId = -1;
    }

    /**
     * This is the full constructor of Peer.
     *
     * @param port
     * @param indexServerIP
     * @param indexServerPort
     */
    public Peer(int port, String indexServerIP, int indexServerPort) {
        this.port = port;
        this.server = new FileDownloadServer(this.port);
        this.server.setDaemon(true);
        this.indexServerIP = indexServerIP;
        this.indexServerPort = indexServerPort;
        this.peerId = -1;
    }

    public long getPeerId() {
        return peerId;
    }

    public void setPeerId(long peerId) {
        this.peerId = peerId;
    }

    /**
     * Get all the filenames in the shared folder. It does not support
     * recursively looking up now.
     *
     * @return
     */
    public String[] getSharedFiles() {
        File shared = new File("shared");
        Iterator<File> iter = FileUtils.iterateFiles(shared, null, false);
        List<String> fileNames = new ArrayList<String>();
        while (iter.hasNext()) {
            fileNames.add(iter.next().getName());
        }
        return (String[]) fileNames.toArray(new String[fileNames.size()]);
    }

    /**
     * There are two cases to use this method: 1) if it is the first time for
     * the peer to register, the peerId is 0; 2) if the peer wants to
     * re-register due to some files changing, the peerId should be the id
     * returned from IndexServer.
     *
     * @param peerId
     * @param files
     * @return peerId
     */
    @Override
    public long registry(long peerId, String[] files) {
        long result = -1L;
        try {
            socket = new Socket(this.indexServerIP, this.indexServerPort);
            P2PProtocol protocol = new P2PProtocol();

            Map<String, Object> parameters = new HashMap<String, Object>();
            if (peerId < 0) {
                parameters.put("peerId", "null");
            } else {
                parameters.put("peerId", peerId);
            }
            parameters.put("port", port);
            parameters.put("files", files);

            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.REG, parameters);
            protocol.preparedOutput(socket.getOutputStream(), messageOut);
            socket.shutdownOutput();

            P2PProtocol.Message messageIn = protocol.processInput(socket.getInputStream());
            if (messageIn.getCmd().equals(P2PProtocol.Command.OK)) {
                result = Math.round((Double)messageIn.getBody());
                this.peerId = result;
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private FileHash.Entry[] list2Array(List list) {
        FileHash fh = new FileHash();
        Entry[] results = new Entry[list.size()];
        for(int i=0;i<list.size();i++) {
            StringMap sm = (StringMap)list.get(i);
            results[i] = fh.new Entry(Math.round((Double)sm.get("peerId")), (String)sm.get("filename"));
        }
        return results;
    }

    @Override
    public FileHash.Entry[] search(String filename) {
        FileHash.Entry[] result = null;
        try {
            socket = new Socket(this.indexServerIP, this.indexServerPort);

            P2PProtocol protocol = new P2PProtocol();
            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.SCH, filename);
            protocol.preparedOutput(socket.getOutputStream(), messageOut);
            socket.shutdownOutput();

            P2PProtocol.Message messageIn = protocol.processInput(socket.getInputStream());
            if (messageIn.getCmd().equals(P2PProtocol.Command.OK)) {
                result = this.list2Array((List) messageIn.getBody());
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public Address lookup(long peerId) {
        Address result = new Address();
        try {
            socket = new Socket(this.indexServerIP, this.indexServerPort);

            P2PProtocol protocol = new P2PProtocol();
            P2PProtocol.Message messageOut = protocol.new Message(P2PProtocol.Command.LOK, peerId);
            protocol.preparedOutput(socket.getOutputStream(), messageOut);
            socket.shutdownOutput();

            P2PProtocol.Message messageIn = protocol.processInput(socket.getInputStream());
            if (messageIn.getCmd().equals(P2PProtocol.Command.OK)) {
                //Create a Address object from StringMap
                StringMap lookResult = (StringMap) messageIn.getBody();
                double portNb = (Double) lookResult.get("port");
                result.setPort((int) portNb);
                String lookHost = (String) lookResult.get("host");
                result.setHost(lookHost);

                //result = (Address) messageIn.getBody();
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public void obtain(String filename) {
        try {
            // Search file entries
            FileHash.Entry[] entries = this.search(filename);
            // Lookup peer's address
            if(entries.length<0) {
                return;
            }
            long remotePeerId = entries[0].getPeerId();
            Address peerAddress = this.lookup(remotePeerId);
            // download the file from the peer
            Socket sock = new Socket(peerAddress.getHost(), peerAddress.getPort());
            P2PReceiver receiver = new P2PReceiver(sock, filename);
            receiver.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void startup() {
        this.server.start();
    }

    @Override
    public void exit() {
        this.server.close();
    }

    @Override
    public boolean ping() {
        return this.server.isAlive() && this.server.isDaemon();
    }

    /**
     * This server is only used for downloading files between peers. If the peer
     * needs to talk with IndexServer, use other built-in methods.
     */
    public class FileDownloadServer extends Thread {

        private final int port;
        private boolean running;
        private ServerSocket listener;

        private FileDownloadServer(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                this.running = true;
                listener = new ServerSocket(port);
                while (running) {
                    Socket sock = listener.accept();
                    P2PSender sender = new P2PSender(sock);
                    sender.start();
                }
            } catch (IOException ex) {
                Logger.getLogger(MyTorrent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void close() {
            this.running = false;
            if (this.listener != null) {
                try {
                    this.listener.close();
                } catch (IOException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
