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
import mytorrent.p2p.P2PSender;

/**
 *
 * This fileServer is only used for downloading files between peers. If the peer
 * needs to talk with IndexServer, use other built-in methods.
 *
 * @author Bo
 */
public class FileServer extends Thread {

    private final int port;
    private boolean running;
    private ServerSocket listener;

    public FileServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        this.running = true;
        try {
            listener = new ServerSocket(port);
            while (running) {
                try {
                    Socket sock = listener.accept();
                    P2PSender sender = new P2PSender(sock);
                    sender.start();
                } catch (SocketException socketException) {
                    Logger.getLogger(FileServer.class.getName()).log(Level.FINE, "socket error", socketException);
                    continue;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, "The server is going down", ex);
            this.running = false;
        }

    }

    public void close() {
        this.running = false;
        if (this.listener != null) {
            try {
                this.listener.close();
            } catch (IOException ex) {
                Logger.getLogger(FileServer.class.getName()).log(Level.SEVERE, "close error", ex);
            }
        }
    }
}
