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
import java.io.PrintWriter;
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
    private ServerSocket ss;           //need cleanup
    private Socket socket;             //need cleanup
    //raw data streams
    private InputStream is;
    private OutputStream os;
    //Line reader and writer used to communicate mainly in run()
    private BufferedReader linereader; //need cleanup
    private PrintWriter linewriter;    //need cleanup
    private String rawcmd;
    private String[] cmd;
    //Status flags
    private boolean RUNNING;

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
        //throw new UnsupportedOperationException("Not supported yet.");

        //print on server console
        System.out.println("Received: " + cmd[0]);
        linewriter.println(cmd[0]);
        //DataOutputStream out = new DataOutputStream(os);
        //out.writeBytes(cmd);
        return true;
    }

    @Override
    public void startup() {
        //throw new UnsupportedOperationException("Not supported yet.");

        //server startup with client socket returned

        //Start up serversocket
        try {

            this.ss = new ServerSocket(port);

        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port + ".");
            System.exit(1);
        }

        //Start up serversocket listening block, until return this.socket
        try {
            socket = null;
            this.socket = ss.accept();
        } catch (IOException e) {
            System.err.println("Accept failed during Startup.");
            System.exit(1);
        }

    }

    @Override
    public void exit() {
        //throw new UnsupportedOperationException("Not supported yet.");

        //clean up
        try {
            linereader.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        linewriter.close();
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            ss.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {


        //this.startup();

        //while (RUNNING) {


            //cmd line communication connection with client
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
                linewriter = new PrintWriter(os, true);
                linereader = new BufferedReader(new InputStreamReader(is));
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: " + socket.getInetAddress().getHostAddress());
                System.exit(1);
            }

            //parse the cmd line input
            try {
                rawcmd = linereader.readLine();
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            cmd = rawcmd.split("\\s");
            if (cmd[0].equals("registry")) {
                //Do registry
            } else if (cmd[0].equals("search")) {
                //Do search
            } else if (cmd[0].equals("ping")) {
                //Do ping
                this.ping();
            } else {
                System.err.println("Command " + cmd[0] + "is not supported!");
            }


/*
            try {
                linewriter.close();
                linereader.close();
                os.close();
                is.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Couldn't close linereader!");
            }


            //Renew the server listening block
            socket = null;
            try {
                this.socket = ss.accept();
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }

*/


        




    }

    public static void main(String[] args) {




        boolean listening = true;
        while (listening) {

            IndexServer is1 = new IndexServer();
            is1.startup();
            new Thread(is1).start();
            is1.exit();
        }

    }
}
