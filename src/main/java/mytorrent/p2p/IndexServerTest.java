/*
 * The MIT License
 *
 * Copyright 2012 ASUS.
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
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.P2PProtocol;
import mytorrent.p2p.P2PProtocol.Command;

/**
 *
 * @author ASUS
 */
public class IndexServerTest {

    public static void main(String[] args) {

        Socket sock = null;

        System.out.println("Begin");

        try {
            sock = new Socket("localhost", 5700);
        } catch (UnknownHostException ex) {
            Logger.getLogger(IndexServerTest.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error 1");
        } catch (IOException ex) {
            Logger.getLogger(IndexServerTest.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error 2");
        }



        //Send Out Pping
        System.out.println("server socket calling made");
        boolean ping = true;
        P2PProtocol pp = new P2PProtocol();
        P2PProtocol.Message outputs = pp.new Message(P2PProtocol.Command.PIG, ping);
        System.out.println(outputs.getCmd());

        System.out.println("ready to preparedoutput()");
        try {
            pp.preparedOutput(sock.getOutputStream(), outputs);
        } catch (IOException ex) {
            Logger.getLogger(IndexServerTest.class.getName()).log(Level.SEVERE, null, ex);
        }



        System.out.println("finished preparedoutput");
        //Wait for ping-OK back

        P2PProtocol.Message in = null;
        try {
            System.out.println("ready to processInput");
            in = pp.processInput(sock.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(IndexServerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (P2PProtocol.Command.OK == in.getCmd()) {
            System.out.println("Test Success");
        } else {
            System.out.println("Test Failed");
        }
        try {
            //clean up
            //is.close();
            //os.close();
            sock.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexServerTest.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error 4");
        }


    }
}
