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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bo Feng
 */
public class P2PSender extends Thread {

    private String filename;
    private Socket socket;

    public P2PSender(Socket sock) throws IOException {
        this.socket = sock;

        InputStream in = socket.getInputStream();
        char[] data = new char[256];
        for (int i = 0; i < data.length; i++) {
            data[i] = (char) in.read();
        }
        this.filename = String.valueOf(data);
        in.close();
    }

    public P2PSender(Socket socket, String filename) {
        this.socket = socket;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            OutputStream os = socket.getOutputStream();
//            Path path = FileSystems.getDefault().getPath("shared", filename);
//            Files.copy(path, os);
            System.out.println("I'm going to send: " + filename);
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(P2PSender.class.getName()).log(Level.SEVERE, filename, ex);
        }
    }
}
