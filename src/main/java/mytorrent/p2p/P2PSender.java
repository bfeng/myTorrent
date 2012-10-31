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
package mytorrent.p2p;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Bo Feng
 */
public class P2PSender extends Thread {

    private String filename;
    private Socket socket;
    private OutputStream os;
    private InputStream is;

    public P2PSender(Socket sock) throws IOException {
        this.socket = sock;


        this.os = socket.getOutputStream();
        this.is = socket.getInputStream();

        byte[] buf = new byte[256];
        StringBuilder sb = new StringBuilder();
        while ((is.read(buf) != -1)) { // todo: check if the filename is too long
            sb.append(new String(buf));
        }
        this.filename = sb.toString();
    }

    public P2PSender(Socket socket, String filename) {
        this.socket = socket;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            File sharedfile = new File("shard/" + filename);
            File receivedfile = new File("received/" + filename);
            int i = 0;
            if (sharedfile.exists()) {
                i = 1;
            }
            if (receivedfile.exists()) {
                i = 2;
            }
            switch (i) {
                case 0:
                    System.out.println("BUG in sender! Attempting to get a non-exsiting file!");
                    break;
                case 1:
                    long numOfBytes = FileUtils.copyFile(new File("shared/" + filename), os);
                    Logger.getLogger(P2PSender.class.getName()).log(Level.INFO, filename, numOfBytes);
                    socket.shutdownOutput();
                    break;
                case 2:
                    long numOfBytes2 = FileUtils.copyFile(new File("received/" + filename), os);
                    Logger.getLogger(P2PSender.class.getName()).log(Level.INFO, filename, numOfBytes2);
                    socket.shutdownOutput();
                    break;
                default:
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(P2PSender.class.getName()).log(Level.SEVERE, filename, ex);
        }
    }
}
