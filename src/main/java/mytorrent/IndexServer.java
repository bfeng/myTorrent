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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.FileHash.Entry;
import mytorrent.p2p.P2PProtocol;
import mytorrent.p2p.P2PProtocol.Command;
import mytorrent.p2p.P2PProtocol.Message;
import mytorrent.p2p.P2PTransfer;

/**
 *
 * @author Bo Feng
 * @author Sizhou Wang
 * @version 1.0
 */
public class IndexServer implements P2PTransfer, Runnable {

    private static final int port = 5700;
    private Socket socket;             //need cleanup
    //raw data streams
    private InputStream is;
    private OutputStream os;
    //Line reader and writer used to communicate mainly in run()
    //private BufferedReader linereader; //need cleanup
    //private PrintWriter linewriter;    //need cleanup
    //private String rawcmd;
    //private String[] cmd;
    //Status flags
    //private boolean RUNNING = true;

    public IndexServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public long registry(int peerId, String[] files) {
        //throw new UnsupportedOperationException("Not supported yet.");

        //Neccessary Information
        String peer = "" + peerId;
        int arraysize = files.length;

        //Loop for the times with respect to the number of files
        int i = 0;
        String ThisFileName;
        String ThisFileRegName;
        boolean exists;
        boolean peerexists;
        PrintWriter pw;
        Scanner sn;
        String theline;
        long returnvalue = 1;
        while (i < arraysize) {

            ThisFileName = files[i];
            ThisFileRegName = ThisFileName + "xx";

            exists = (new File(ThisFileRegName)).exists();
            pw = null;
            sn = null;

            if (!exists) {
                try {
                    pw = new PrintWriter(new FileWriter(new File(ThisFileRegName)));
                    pw.println(peer);
                    pw.close();
                } catch (IOException ex) {
                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                    returnvalue = 1;
                }

            } else {
                try {
                    sn = new Scanner(new FileReader(ThisFileRegName));
                    //Loop for peer in each line in the file (Scanner)
                    peerexists = false;
                    while ((sn.hasNextLine()) && !peerexists) {
                        theline = sn.nextLine();
                        if (theline.startsWith(peer)) {
                            peerexists = true;
                        }
                    }
                    sn.close();
                    if (!peerexists) {
                        try {
                            pw = new PrintWriter(new FileWriter(new File(ThisFileRegName), true));
                            pw.println(peer);
                            pw.close();
                        } catch (IOException ex) {
                            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                            returnvalue = 2;
                        }

                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                    returnvalue = 3;
                }

            }
            //proceed to the next file
            i++;
        }
        //returnvalue
        returnvalue = 0;

        return returnvalue;
    }

    @Override
    public Entry[] search(String filename) {
        //throw new UnsupportedOperationException("Not supported yet.");\

        Entry[] returnEntry = null;
        boolean exists = (new File(filename)).exists();
        if (exists) {
            try {
                Scanner sn = new Scanner(new FileReader(filename));
                //Count line number
                int i = 0;
                while (sn.hasNextLine()) {
                    sn.next();
                    i++;
                }
                sn.close();
                sn = new Scanner(new FileReader(filename));
                String[] Peers = new String[i];
                //Entry stuff
                returnEntry = new Entry[i];
                long templong;
                for (int k = i; k > 0; k--) {
                    templong = Long.parseLong(Peers[k - 1]);
                    returnEntry[k - 1].setPeerId(templong);
                    returnEntry[k - 1].setFilename(filename);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnEntry;

    }

    @Override
    public boolean ping() {
        throw new UnsupportedOperationException("Not supported yet.");
        /*
        //print on server console
        System.out.println(" ");
        System.out.println("Received: " + cmd[0]);
        System.out.print(">>>");
        linewriter.println(cmd[0]);
        //DataOutputStream out = new DataOutputStream(os);
        //out.writeBytes(cmd);
        return true;
         * 
         */
    }

    @Override
    public void startup() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exit() {
        throw new UnsupportedOperationException("Not supported yet.");

        //clean up
        /*try {
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
         * 
         */

    }

    @Override
    public void run() {

        //Waiting for 
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + socket.getInetAddress().getHostAddress());
            System.exit(1);
        }

        //waiting for input
        P2PProtocol ppin = new P2PProtocol();
        Message input = ppin.processInput(is);
        Command inputcmd = input.getCmd();

        //Process command and then open input.GetObject
        if (inputcmd == Command.REG) {
            //Do registry
            Entry[] RegEntry = (Entry[])input.getBody();
            int peerid = (int) RegEntry[0].getPeerId();
            int arraysize = RegEntry.length;
            String[] filenames = new String[arraysize];
            for (int i = arraysize;i>0;i--){
                filenames[i] = RegEntry[i].getFilename();
            }
            long returnReg = this.registry(peerid, filenames);

            //Send to client work DONE
            P2PProtocol ppout = new P2PProtocol();
            Message output = ppout.new Message(Command.OK, returnReg);
            ppout.preparedOutput(os, output);

        } else if (inputcmd == Command.SCH) {
            //Do search

            String filename1 = (String) input.getBody();
            Entry[] ReturnEntry = this.search(filename1);

            //Send to client back
            P2PProtocol ppout = new P2PProtocol();
            Message output = ppout.new Message(Command.OK, ReturnEntry);
            ppout.preparedOutput(os, output);



        } /*else if (cmd[0].equals("ping")) {
        //Do ping
        this.ping();
        }  else {
        System.err.println("Command " + cmd[0] + "is not supported!");
        }
         * 
         */

        //Clean up finished connection
        try {
            //linewriter.close();
            //linereader.close();
            os.close();
            is.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Couldn't close linereader!");
        }

    }

    public static void main(String[] args) {

        //Set up Shell
        new Thread(new IndexServerShell()).start();

        //Startup Server-ServerSocket;
        //server startup with client socket returned
        //Start up serversocket
        boolean listening = true;
        try {
            ServerSocket ss = new ServerSocket(port);
            while (listening) {
                new Thread(new IndexServer(ss.accept())).start();
            }
            ss.close();
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port + ".");
            System.exit(1);
        }
    }
}

class IndexServerShell implements Runnable {

    @Override
    public void run() {
        //Server Console Shell
        while (true) {
            System.out.print(">>>");
            Scanner userScanner = new Scanner(System.in);
            String userInputRaw = userScanner.nextLine();
            System.out.println("Echo: " + userInputRaw);
        }
    }
}
