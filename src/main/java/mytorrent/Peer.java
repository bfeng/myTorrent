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
import mytorrent.p2p.FileHash;
import mytorrent.p2p.P2PProtocol;
import mytorrent.p2p.P2PReceiver;
import mytorrent.p2p.P2PTransfer;
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
public class Peer implements P2PTransfer {

    private final FileServer fileServer;
    private final IndexServer indexServer;
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

    @Override
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

    @Override
    public void exit() {
        this.fileServer.close();
    }

    @Override
    public boolean ping() {
        return this.fileServer.isAlive() && this.fileServer.isDaemon();
    }

    @Override
    public void query(String filename, long messageID, int TTL) {
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
        int timer = 60; // the maximum seconds it can wait
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
                    break;
                }

                if (timer-- < 0) {
                    break;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }


//        try {
//            BannerManager.QueryWaiting();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        //#-4
        //get return value struct
        FileHash.Entry[] returnStruct = indexServer.returnRemoteFileHash_for(filename);
        BannerManager.printSearchReturns(returnStruct);


    }

    @Override
    public void obtain(final String filename, final int messageID, final int TTL) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            query(filename, messageID, TTL);
                            long[] results = indexServer.getQueryResult(filename);
                            PeerAddress pa = indexServer.getPeerAddress(results[0]);
                            Socket socket = new Socket(pa.getPeerHost(), pa.getFileServerPort());
                            new P2PReceiver(socket, filename).start();
                        } catch (IOException ex) {
                            Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, "obtain", ex);
                        }
                    }
                }).start();
    }
}
