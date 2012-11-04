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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import mytorrent.p2p.P2PProtocol;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

/**
 *
 * @author bfeng
 */
public class VersionMonitorTest {

    private InputStream in;
    private OutputStream out;
    private P2PProtocol protocol;
    public int flag;
    public ConcurrentHashMap<String, FileBusinessCard> Push_file_map;
    public ArrayDeque<String> Push_broadcast_external;

    public VersionMonitorTest() {
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
    public void test_FileChange() throws FileNotFoundException, FileSystemException {
        try {
            flag = 0;
            FileSystemManager fsManager = VFS.getManager();
            FileObject listendir = fsManager.resolveFile(new File("shared/").getAbsolutePath());
            DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {

                @Override
                public void fileCreated(FileChangeEvent fce) throws Exception {
                }

                @Override
                public void fileDeleted(FileChangeEvent fce) throws Exception {
                }

                @Override
                public void fileChanged(FileChangeEvent fce) throws Exception {
                    flag = 1;
                }
            });
            fm.setRecursive(false);
            fm.addFile(listendir);
            fm.start();
            Thread.sleep(10000);
            assertEquals(flag, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersionMonitorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void test_FileDelete() throws FileNotFoundException, FileSystemException {
        try {
            flag = 0;
            FileSystemManager fsManager = VFS.getManager();
            FileObject listendir = fsManager.resolveFile(new File("shared/").getAbsolutePath());
            DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {

                @Override
                public void fileCreated(FileChangeEvent fce) throws Exception {
                }

                @Override
                public void fileDeleted(FileChangeEvent fce) throws Exception {
                    flag = 1;
                }

                @Override
                public void fileChanged(FileChangeEvent fce) throws Exception {
                }
            });
            fm.setRecursive(false);
            fm.addFile(listendir);
            fm.start();
            Thread.sleep(10000);
            assertEquals(flag, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersionMonitorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void test_FileCreate() throws FileNotFoundException, FileSystemException {
        try {
            flag = 0;
            FileSystemManager fsManager = VFS.getManager();
            FileObject listendir = fsManager.resolveFile(new File("shared/").getAbsolutePath());
            DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {

                @Override
                public void fileCreated(FileChangeEvent fce) throws Exception {
                    flag = 1;
                }

                @Override
                public void fileDeleted(FileChangeEvent fce) throws Exception {
                }

                @Override
                public void fileChanged(FileChangeEvent fce) throws Exception {
                }
            });
            fm.setRecursive(false);
            fm.addFile(listendir);
            fm.start();
            Thread.sleep(10000);
            assertEquals(flag, 1);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersionMonitorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void test_UpdateConsistencyData() throws FileNotFoundException, FileSystemException {
        Push_file_map.clear();
        try {
            flag = 0;
            FileSystemManager fsManager = VFS.getManager();
            FileObject listendir = fsManager.resolveFile(new File("shared/").getAbsolutePath());
            DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {

                @Override
                public void fileCreated(FileChangeEvent fce) throws Exception {
                    FileBusinessCard newCard = new FileBusinessCard("filename", 101, "localhost", 1, 2);
                    Push_file_map.put("filename", newCard);
                }

                @Override
                public void fileDeleted(FileChangeEvent fce) throws Exception {
                }

                @Override
                public void fileChanged(FileChangeEvent fce) throws Exception {
                }
            });
            fm.setRecursive(false);
            fm.addFile(listendir);
            fm.start();
            Thread.sleep(10000);
            FileBusinessCard theCard = new FileBusinessCard("filename", 101, "localhost", 1, 2);
            assertEquals(Push_file_map.get("filename"), theCard);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersionMonitorTest.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    @Test
    public void test_ExternalJobStack() throws FileNotFoundException, FileSystemException {
        Push_broadcast_external.clear();
        try {
            flag = 0;
            FileSystemManager fsManager = VFS.getManager();
            FileObject listendir = fsManager.resolveFile(new File("shared/").getAbsolutePath());
            DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {

                @Override
                public void fileCreated(FileChangeEvent fce) throws Exception {
                    Push_broadcast_external.offer("filename");
                }

                @Override
                public void fileDeleted(FileChangeEvent fce) throws Exception {
                }

                @Override
                public void fileChanged(FileChangeEvent fce) throws Exception {
                }
            });
            fm.setRecursive(false);
            fm.addFile(listendir);
            fm.start();
            Thread.sleep(10000);
            String poll = Push_broadcast_external.poll();
            assertEquals(poll, "filename");
        } catch (InterruptedException ex) {
            Logger.getLogger(VersionMonitorTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
