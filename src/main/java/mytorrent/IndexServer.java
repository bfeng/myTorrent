/*
 * The MIT License
 *
 * Copyright 2012 bfeng5.
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.FileHash.Entry;
import mytorrent.p2p.P2PTransfer;

/**
 *
 * @author Bo Feng
 * @version 1.0
 */
public class IndexServer implements P2PTransfer, Runnable {

    private static final int port = 5700;
    private Socket socket;

    @Override
    public long registry(int peerId, String[] files) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entry[] search(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean ping() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startup() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run() {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String cmd = br.readLine();
            
            System.out.println("Received: " + cmd);
            
            DataOutputStream out = new DataOutputStream(os);
            out.writeBytes(cmd);
        } catch (IOException ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(port);
            boolean listening = true;
            while (listening) {
                Socket sock = ss.accept();
                IndexServer is = new IndexServer();
                is.socket = sock;
                new Thread(is).start();
            }
            ss.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
