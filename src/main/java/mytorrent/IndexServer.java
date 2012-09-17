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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.Address;
import mytorrent.p2p.FileHash;
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
    private static HashMap<Long, Address> AddressTable;
    private static Address TheAddress;
    private static FileHash filehashdepot;

    public IndexServer(Socket socket) {
        this.socket = socket;
        IndexServer.AddressTable = new HashMap<Long, Address>();
        IndexServer.filehashdepot = new FileHash();
        IndexServer.TheAddress = null;
    }

    //Static Field Accesser
    //#
    //AddressTable Accesser
    private static Address LOKAddressTable(long peerId) {
        TheAddress = null;
        if (AddressTable.containsKey(peerId)) {
            TheAddress = AddressTable.get(peerId);
        }
        return TheAddress;
    }

    private static void RegisterAddressTable(long peerId, Address peerAddress) {
        AddressTable.put(peerId, peerAddress);
    }
    
    private long registry(long peerId, List<String> filesArrayList) {
        String[] files = new String[filesArrayList.size()];
        for(int i = 0;i<files.length;i++) {
            files[i] = filesArrayList.get(i);
        }
        return this.registry(peerId, files);
    }

    @Override
    public long registry(long peerId, String[] files) {
        //throw new UnsupportedOperationException("Not supported yet.");
        //registry register all files respect to peerId onto filehashdeport;
        //It does need to call FileHash methods and this. static methods
        //input: long peerId 
        //       String[]files
        //return: long returnReg (Not useful. It should be error indicator or just peerId rewind)
        System.out.println("Received: REG");
        //##
        //REG-First
        //Delete all existing files for this peerId even if there is none
        filehashdepot.removeAll(peerId);
        //##
        //REG-Second
        //Reconstruct all files for this peerId
        for (String item : files) {
            filehashdepot.addEntry(filehashdepot.new Entry(peerId, item));
        }
        return peerId;

    }

    @Override
    public Entry[] search(String filename) {
        //throw new UnsupportedOperationException("Not supported yet.");\
        //##
        //Search and return Entry use existing mechod
        return filehashdepot.search(filename);

    }

    @Override
    public Address lookup(long peerId) {
        //throw new UnsupportedOperationException("Not supported yet.");
        return LOKAddressTable(peerId);
    }

    @Override
    public boolean ping() {
        //throw new UnsupportedOperationException("Not supported yet.");
        //print on server console
        System.out.println(" ");
        System.out.println("Received: PIG");
        //System.out.print(">>>");
        return true;

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
        P2PProtocol pp = new P2PProtocol();
        P2PProtocol.Message inputs = null;
        //Waiting for 
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
            //waiting for input
            inputs = pp.processInput(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + socket.getInetAddress().getHostAddress());
            System.exit(1);
        }
        P2PProtocol.Command inputcmd = inputs.getCmd();

        //Process command and then open input.GetObject
        if (inputcmd == Command.REG) {
            //Do registry
            //Incomming Message for REG contains a body of Map<String, Object>:
            // (1)"peerId" : String "null" or peerId;
            // (2)"port" : int port;
            // (3)"files" : String[]files;
            //###
            //First, register peerId options:
            // (peerId == "null"): register a long newpeerId in the AddressTable<Long, Address>;
            // (peerId != "null"): skip this step;
            //@SuppressWarnings("all")
            Map<String, Object> inputMessagebody = (Map<String, Object>) inputs.getBody();
            long newpeerId = 0;
            if ("null".equals(inputMessagebody.get("peerId"))) {
                //generate a number that doesn't exist in the AddressTable
                newpeerId = 10001;
                while (LOKAddressTable(newpeerId) != null) {
                    newpeerId++;
                }
                //generate newpeerAddress
                Address newpeerAddress = new Address();
                newpeerAddress.setHost(socket.getInetAddress().getHostAddress());
                newpeerAddress.setPort(socket.getLocalPort());
                //register AddressTable with the newpeerId now
                RegisterAddressTable(newpeerId, newpeerAddress);
            } else {
                newpeerId = Long.valueOf((String)inputMessagebody.get("peerId")).longValue();
            }
            //###
            //Second parse the files[] and call registry() method to continue
            long returnReg = this.registry(newpeerId, (List<String>) inputMessagebody.get("files"));
            //###
            //Third handle return Message
            // (1) peer expect to see Command.OK
            // (2) peer expect to see newpeerId;
            returnReg = newpeerId; //This line is not neccessary
            P2PProtocol.Message output = pp.new Message(Command.OK, returnReg);
            try {
                pp.preparedOutput(socket.getOutputStream(), output);
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (inputcmd == Command.SCH) {
            //Do search
            //Incomming Message for SCH contains only one String filename;
            //And expect an Entry[] containing all files
            //###
            //First call search() method using filename
            FileHash.Entry[] ReturnEntry = this.search((String) inputs.getBody());

            //###
            //Second handle return Entry[]
            P2PProtocol.Message output = pp.new Message(Command.OK, ReturnEntry);
            try {
                pp.preparedOutput(socket.getOutputStream(), output);
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }



        } else if (inputcmd == Command.PIG) {
            //Do ping
            boolean ping = this.ping();
            P2PProtocol.Message output = pp.new Message(Command.OK, ping);
            try {
                pp.preparedOutput(socket.getOutputStream(), output);
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (inputcmd == Command.LOK) {
            //Do lookup
            //Incomming Message for LOK contains only long peerId
            //Expecting Command.OK and Address with respect to the peerId
            P2PProtocol.Message output = pp.new Message(Command.OK, this.lookup((Long)inputs.getBody()));
            try {
                pp.preparedOutput(socket.getOutputStream(), output);
            } catch (IOException ex) {
                Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            P2PProtocol.Message output = pp.new Message(Command.ERR, "Command is not supported!");
            pp.preparedOutput(os, output);
        }
        //Clean up finished connection
        try {
            os.close();
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        boolean listening = true;
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Index Server is Running.");
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
