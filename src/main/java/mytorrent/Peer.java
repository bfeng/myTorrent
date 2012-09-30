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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.Configuration;
import mytorrent.p2p.P2PProtocol;
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
    private final PeerAddress hostAddress;

    /**
     * This is the constructor of Peer.
     *
     * @param fileServerPort
     */
    public Peer() throws FileNotFoundException {
        this("config.yaml");
    }

    /**
     * This is the full constructor of Peer.
     *
     * @param filepath
     */
    public Peer(String filepath) throws FileNotFoundException {

        Configuration config = Configuration.load(filepath);

        this.hostAddress = config.getHostAddress();

        this.fileServer = new FileServer(this.hostAddress.getFileServerPort());
        this.fileServer.setDaemon(true);

        this.indexServer = new IndexServer(this.hostAddress.getIndexServerPort());
        this.indexServer.setDaemon(true);

        this.neighbors = config.getNeighbors();
    }

    public long getPeerId() {
        return this.hostAddress.getPeerID();
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

    private boolean checkMessage(P2PProtocol.Message in) {
        if (in != null && in.getCmd() != null & in.getCmd().equals(P2PProtocol.Command.OK)) {
            return true;
        }
        return false;
    }

    @Override
    public void obtain(String filename) {
    }

    @Override
    public void startup() {
        try {
            this.fileServer.start();

            FileSystemManager fsManager = VFS.getManager();
            FileObject listendir = fsManager.resolveFile(new File("shared/").getAbsolutePath());
            DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {
                private synchronized void update() {
                    // update its filehash
                    //, so that the query by neighbors could update
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
    public void query() {
        throw new UnsupportedOperationException("Not supported yet.");

        // query its neighbors
    }

    @Override
    public void hitquery() {
        throw new UnsupportedOperationException("Not supported yet.");

        // return query to its neighbors
    }
}
