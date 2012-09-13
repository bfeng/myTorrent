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
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
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
    private boolean RUNNING = true;

    @Override
    public long registry(int peerId, String[] files) {
        //throw new UnsupportedOperationException("Not supported yet.");

        //Neccessary Information
        String peer = "" + peerId;
        int arraysize = files.length;



        //Loop for the times with respect to the number of files
        int i = 0;
        String ThisFileName = null;
        String ThisFileRegName = null;
        boolean exists;
        boolean peerexists;
        PrintWriter pw;
        Scanner sn;
        String theline;
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
                        }

                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            //proceed to the next file
            i++;
        }


        /*
        File thefile = new File("\registry.txt");
        boolean success = thefile.createNewFile();
        PrintWriter filewriter = new PrintWriter(new FileWriter(thefile));
        if (success) {
        for (int i = 0; i < arraysize; i++) {
        filewriter.println(files[i] + " " + peer);
        }
        } else {
        Scanner filescanner = new Scanner(new FileReader(thefile));
        int i = 0;
        String theline = null;
        String newline;
        boolean exist;
        
        //Loop for the times with respect to the number of files
        while (i < arraysize) {
        
        exist = false;
        //loop until the line with the file name is found
        while ((filescanner.hasNextLine()) && !exist) {
        
        theline = filescanner.nextLine();
        if (theline.startsWith(files[i])) {
        exist = true;
        }
        }
        //loop complete with either EOF or file found
        if (exist) {
        //line with the filename is found
        //identify if the current peerId is on the line
        if (theline.contains(peer)) {
        //already registered, just break
        //do nothing
        } else {
        //append the peerId in the line
        newline = theline + " " + peer;
        filewriter.println(newline);
        }
        } else {
        //not exist, need create new line with the file name
        filewriter.println(files[i] + " " + peer);
        }
        
        //proceed to the next file
        i++;
        }
        }
         */




        long returnvalue = 0;

        return returnvalue;
    }

    @Override
    public Entry[] search(String filename) {
        //throw new UnsupportedOperationException("Not supported yet.");

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
                //Entry stuff


            } catch (FileNotFoundException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }



        }





    }

    @Override
    public boolean ping() {
        //throw new UnsupportedOperationException("Not supported yet.");

        //print on server console
        System.out.println(" ");
        System.out.println("Received: " + cmd[0]);
        System.out.print(">>>");
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

        while (RUNNING) {

            //Start up server listening block, until return this.socket
            try {
                //socket = null;
                this.socket = ss.accept();
            } catch (IOException e) {
                System.err.println("Accept failed during Startup.");
                System.exit(1);
            }

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

            //Send to client ACK
            linewriter.println("ACK");

            //Process command
            cmd = rawcmd.split("\\s");
            int cmdSize = cmd.length;
            if (cmd[0].equals("registry")) {
                //Do registry
                int peerid = Integer.parseInt(cmd[1]);
                String[] filenames = Arrays.copyOfRange(cmd, 2, cmdSize - 1);
                long returnReg = this.registry(peerid, filenames);
                //Send to client work DONE
                linewriter.println(returnReg);

            } else if (cmd[0].equals("search")) {
                //Do search
                String filename1 = cmd[1];
                Entry[] ReturnEntry = this.search(filename1);

                //Send to client Entry



            } else if (cmd[0].equals("ping")) {
                //Do ping
                this.ping();
            } else {
                System.err.println("Command " + cmd[0] + "is not supported!");
            }

            //Clean up finished connection
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
        }
    }

    public static void main(String[] args) {


        //Set up indexserver 1
        IndexServer is1 = new IndexServer();
        is1.startup();
        new Thread(is1).start();

        //Server Console Shell
        while (true) {
            System.out.print(">>>");
            Scanner userScanner = new Scanner(System.in);
            String userInputRaw = userScanner.nextLine();
            System.out.println("Echo: " + userInputRaw);
        }


    }
}
