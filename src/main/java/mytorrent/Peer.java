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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.gui.BannerManager;
import mytorrent.p2p.Configuration;
import mytorrent.p2p.FileBusinessCard;
import mytorrent.p2p.FileHash;
import mytorrent.p2p.P2PProtocol;
import mytorrent.p2p.P2PReceiver;
import mytorrent.p2p.PeerAddress;
import mytorrent.peer.FileServer;
import mytorrent.peer.IndexServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

/**
 *
 * @author Bo Feng
 * @version 2.0
 */
public class Peer {

    private final FileServer fileServer;
    public final IndexServer indexServer;
    private final PeerAddress[] neighbors;
    private final PeerAddress host;

    /**
     * This is the constructor of Peer.
     *
     * @param fileServerPort
     */
    public Peer() throws FileNotFoundException {
        this("config.local.yml");
    }

    /**
     * This is the full constructor of Peer.
     *
     * @param filepath
     */
    public Peer(String filepath) throws FileNotFoundException {
        Configuration config = Configuration.load(filepath);

        this.host = config.getHostAddress();
        this.neighbors = config.getNeighbors();

        this.fileServer = new FileServer(this.host.getFileServerPort());
        this.fileServer.setDaemon(true);

        this.indexServer = new IndexServer(this.host, this.neighbors);
        this.indexServer.setDaemon(true);
    }

    public long getPeerId() {
        return this.host.getPeerID();
    }

    /**
     * Get all the filenames in the shared folder. It does not support
     * recursively looking up now.
     *
     * @return
     */
    private String[] getSharedFiles() {
        File shared = new File("shared");
        Iterator<File> iter = FileUtils.iterateFiles(shared, null, false);
        List<String> fileNames = new ArrayList<String>();
        while (iter.hasNext()) {
            fileNames.add(iter.next().getName());
        }
        return (String[]) fileNames.toArray(new String[fileNames.size()]);
    }

    public void startup() {
        try {
            this.indexServer.start();
            this.fileServer.start();

            this.indexServer.updateFileHash(this.getSharedFiles());

            FileSystemManager fsManager = VFS.getManager();
            FileObject listendir = fsManager.resolveFile(new File("shared/").getAbsolutePath());
            DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {

                private synchronized void update() {
                    indexServer.updateFileHash(getSharedFiles());
                }

                @Override
                public void fileCreated(FileChangeEvent fce) throws Exception {
                    this.update();
                }

                @Override
                public void fileDeleted(FileChangeEvent fce) throws Exception {
                    this.update();
                }

                @Override
                public void fileChanged(FileChangeEvent fce) throws Exception {
                    this.update();
                }
            });
            fm.setRecursive(false);
            fm.addFile(listendir);
            fm.start();
        } catch (FileSystemException ex) {
            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exit() {
        this.fileServer.close();
    }

    public boolean ping() {
        return this.fileServer.isAlive() && this.fileServer.isDaemon();
    }

    private void innerQuery(String filename, long messageID, int TTL) {
        P2PProtocol protocol = new P2PProtocol();
        //#-1
        //generate Query Msg
        P2PProtocol.QueryMessage initQueryMsg = protocol.new QueryMessage(host.getPeerID(), messageID, TTL);
        initQueryMsg.setFilename(filename);
        //generate output MSG
        P2PProtocol.Message initQueryMsgOut = protocol.new Message(initQueryMsg);
        //init indexserver return value struct
        indexServer.initUpdateRemoteFileHash_for(filename);
        //#-2
        //send them out to neighbours
        String aNeighborHost = null;
        int aNeighborPort = -1;
        for (PeerAddress aNeighbor : neighbors) {
            try {
                aNeighborHost = aNeighbor.getPeerHost();
                aNeighborPort = aNeighbor.getIndexServerPort();
                Socket socket = new Socket(aNeighborHost, aNeighborPort);
                protocol.preparedOutput(socket.getOutputStream(), initQueryMsgOut);
                socket.shutdownOutput();
            } catch (UnknownHostException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //#-3
        //waiting and printing

        Set<Long> found = new CopyOnWriteArraySet<Long>();
        int size = 0;
        int timer = 5; // the maximum seconds it can wait
        while (true) {
            try {
                Thread.sleep(1000);
                long[] results = indexServer.getQueryResult(filename);
                if (results != null) {
                    for (long r : results) {
                        found.add(r);
                    }
                }

                if (found.size() > size) {
                    size = found.size();
                } else {
                    if (size > 0) {
                        break;
                    }
                }

                if (timer-- < 0) {
                    break;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

    public void query(String filename, long messageID, int TTL, boolean showoff) {

        this.innerQuery(filename, messageID, TTL);

        if (!showoff) {
            //get return value struct
            FileHash.Entry[] returnStruct = indexServer.returnRemoteFileHash_for(filename);
            BannerManager.printSearchReturns(returnStruct);
        }
    }

    public void obtain(final String filename, final int messageID, final int TTL) {

        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            innerQuery(filename, messageID, TTL);
                            long[] results = indexServer.getQueryResult(filename);
                            if (results == null || results.length == 0) {
                                return;
                            }
                            PeerAddress pa = indexServer.getPeerAddress(results[0]);
                            Socket socket = new Socket(pa.getPeerHost(), pa.getFileServerPort());
                            new P2PReceiver(socket, filename).start();
                        } catch (IOException ex) {
                            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, "obtain", ex);
                        }
                    }
                }).start();
    }

    /*
    
    Master copy of original file in one peer, as original server. Only the master copy can be modified. 
    The original file has version number, which is increased by '1' after each modification.
    PUSH:
    (1) As soon as original file modified, the original server broadcast invalidate message for the file, propagates like query. 
    (2)Each peer checks if it has the file, and invalidate them. 
    (3)Master server does NOT maintain list of owner peers. 
    1) INVALIDATION msg
    2) file state in peer: valid | invalid | TTR expired
    PULL:
    (1) Owner peer poll the original server for version. And server returns confirmation message. 
    (2) TTR is setted by original server and attached with each copy. 
    (3) eager or lazy manner of polling from each peer. 
    
    ----------------------------------------------------------------------------------------------------------------------------
    An original server has file "foo.txt"
    (1) It sets the file rule as either PUSH or PULL
    (2) Any peer attempting to have the file must follow the rule.
    
    PUSH: download -> broadcast -> INVALIDATE
    PULL: download -> poll -> INVALIDATE
    
    <GENERAL>
    Switch or setup pull or push for each file
    <PUSH>
    - <shared FILE list>
    filename
    server info
    state:VALID | INVALID
    - <methods>
    filelistener()
    modification detector(push-list, )
    broadcast()
    - <COMMUNICATION>
    query->; <-pull or push setup msg; pull or push ok ->; <- file
    invalidation ->
    
    <PULL>
    - <received FILE list>
    filename
    server info
    state: VALID | INVALID | TTLEXPIRE
    version
    - <methods>
    modification detector(poll-list, )
    check file state(poll-list, )
    poll(poll-list, )
    answer poll()
    
    - <COMMUNICATION>
    obtain.query->; <-pull or push setup msg; pull or push ok ->; <- file
    poll ->; <- version number
    
    --------------------------------------------------------------------------------------------------------------------------
    [Own File Hash] key = filename (cannot query your own file)
    [Downloaded File Hash] key = filename
    [PUSH list] shared file sets for PUSH
    [POLL list] 
    
    upon receiving a query(obtain), it (1)check [Own File Hash] then (2)[Downloaded File Hash] for file setup
    upon receiving a POLL msg, it checkes [Own File Hash] for version and answer it. 
    upon receiving a PUSH.INVALIDATE msg, it update the file info in [Downloaded File Hash]
    
    
     */
    private void innerQuery2(String filename, long messageID, int TTL) {
        P2PProtocol protocol = new P2PProtocol();
        //#-1
        //generate Query Msg
        P2PProtocol.QueryMessage initQueryMsg = protocol.new QueryMessage(host.getPeerID(), messageID, TTL);
        initQueryMsg.setFilename(filename);
        //generate output MSG
        P2PProtocol.Message initQueryMsgOut = protocol.new Message(initQueryMsg);
        initQueryMsgOut.setCmd(P2PProtocol.Command.QUERYMSG2);
        //init indexserver return value struct
        indexServer.initUpdateRemoteFileHash_for(filename);
        //#-2
        //send them out to neighbours
        String aNeighborHost = null;
        int aNeighborPort = -1;
        for (PeerAddress aNeighbor : neighbors) {
            try {
                aNeighborHost = aNeighbor.getPeerHost();
                aNeighborPort = aNeighbor.getIndexServerPort();
                Socket socket = new Socket(aNeighborHost, aNeighborPort);
                protocol.preparedOutput(socket.getOutputStream(), initQueryMsgOut);
                socket.shutdownOutput();
            } catch (UnknownHostException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //#-3
        //waiting and printing

        Set<Long> found = new CopyOnWriteArraySet<Long>();
        int size = 0;
        int timer = 5; // the maximum seconds it can wait
        while (true) {
            try {
                Thread.sleep(1000);
                long[] results = indexServer.getQueryResult(filename);
                if (results != null) {
                    for (long r : results) {
                        found.add(r);
                    }
                }

                if (found.size() > size) {
                    size = found.size();
                } else {
                    if (size > 0) {
                        break;
                    }
                }

                if (timer-- < 0) {
                    break;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

    public void query2(String filename, long messageID, int TTL, boolean showoff) {

        this.innerQuery2(filename, messageID, TTL);

        if (!showoff) {
            //get return value struct
            FileHash.Entry[] returnStruct = indexServer.returnRemoteFileHash_for(filename);
            BannerManager.printSearchReturns(returnStruct);
        }
    }

    public void obtain2(final String filename, final int messageID, final int TTL) {

        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            innerQuery2(filename, messageID, TTL);
                            long[] results = indexServer.getQueryResult(filename);
                            if (results == null || results.length == 0) {
                                return;
                            }
                            PeerAddress pa = indexServer.getPeerAddress(results[0]);
                            //#-1 Ask for Card
                            P2PProtocol protocol = new P2PProtocol();
                            //generate Query Msg
                            P2PProtocol.QueryMessage initCARDMsg = protocol.new QueryMessage(host.getPeerID(), messageID, TTL);
                            initCARDMsg.setFilename(filename);
                            //generate output MSG
                            P2PProtocol.Message initCARDMsgOut = protocol.new Message(initCARDMsg);
                            initCARDMsgOut.setCmd(P2PProtocol.Command.CARD);
                            //send out
                            Socket socketCard = new Socket(pa.getPeerHost(), pa.getIndexServerPort());
                            protocol.preparedOutput(socketCard.getOutputStream(), initCARDMsgOut);
                            socketCard.shutdownOutput();
                            //wait for input
                            P2PProtocol.Message msgIn = protocol.processInput(socketCard.getInputStream());
                            if (msgIn.getCmd() != P2PProtocol.Command.OK) {
                                System.out.println("Error in asking for FileBusinessCard!");
                                return;
                            }
                            //process the card
                            FileBusinessCard theCard = msgIn.getFileBusinessCard();
                            theCard.set_state(FileBusinessCard.State.VALID);
                            indexServer.versionMonitor.p2p_file_map.put(filename, theCard);

                            //#-2 Ask for file
                            Socket socket = new Socket(pa.getPeerHost(), pa.getFileServerPort());
                            new P2PReceiver(socket, filename).start();
                        } catch (IOException ex) {
                            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, "obtain", ex);
                        }
                    }
                }).start();
    }
}