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
package mytorrent.peer;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.FileBusinessCard;
import mytorrent.p2p.FileBusinessCard.Approach;
import mytorrent.p2p.PeerAddress;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.FileObject;

/**
 *
 * @author swang
 */
public class VersionMonitor extends Thread {

    PeerAddress host;
    FileSystemManager fsManager;
    DefaultFileMonitor Push_fm;
    DefaultFileMonitor Pull_fm;
    public ArrayDeque<String> Push_broadcast_external; //inter thread communication for push
    public ArrayDeque<String> Pull_poll_external; //inter thread communication for pull
    public ConcurrentHashMap<String, FileBusinessCard> Push_file_map;
    public ConcurrentHashMap<String, FileBusinessCard> p2p_file_map;
    TTR_Timer Ttr_timer;

    public VersionMonitor(PeerAddress host_me) {

        //VFS: need one
        try {
            fsManager = VFS.getManager();
        } catch (FileSystemException ex) {
            Logger.getLogger(VersionMonitor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error create VFS.manager!");
        }

        //Need two monitor with different Listener for each peer
        Push_fm = new DefaultFileMonitor(new PushFileListener());
        Pull_fm = new DefaultFileMonitor(new PullFileListener());

        //static values
        this.host = host_me;
        this.Push_broadcast_external = new ArrayDeque<String>();
        this.Pull_poll_external = new ArrayDeque<String>();
        this.Push_file_map = new ConcurrentHashMap<String, FileBusinessCard>();
        this.p2p_file_map = new ConcurrentHashMap<String, FileBusinessCard>();
        
        //one ttr_timer
        Ttr_timer = new TTR_Timer();

    }

    @Override
    public void run() {
        try {
            init();
        } catch (Exception ex) {
            Logger.getLogger(VersionMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        System.out.println("VersionMonitor Started!");


    }

    private void init() throws Exception {

        //shared for push monitor
        FileObject initfiles_push = fsManager.resolveFile(new File("shared/").getAbsolutePath());
        Push_fm.addFile(initfiles_push);
        Push_fm.setRecursive(false);
        Push_fm.start();

        File temp = new File("shared/");
        init_register_push(temp.list(), Push_file_map);
        /*
        for (String item : temp2.list()) {
        System.out.println(item);
        }
         */


        //received for pull monitor
        FileObject initfiles_pull = fsManager.resolveFile(new File("received/").getAbsolutePath());
        Pull_fm.addFile(initfiles_pull);
        Pull_fm.setRecursive(false);
        Pull_fm.start();
        /*
        File temp2 = new File("received/");
        for (String item : temp2.list()) {
        System.out.println(item);
        }
        
         */

        //static init
        Push_broadcast_external.clear();
        
        Ttr_timer.start();

    }

    //init helpers
    private void init_register_push(String[] files, ConcurrentHashMap<String, FileBusinessCard> the_map) throws Exception {

        if (!the_map.isEmpty()) {
            System.out.println("WARNING: VersionMonitor hashmap init before clearning.");
        }

        FileBusinessCard tempCard = null;
        for (String item : files) {
            tempCard = new FileBusinessCard(item, this.host.getPeerID(), this.host.getPeerHost(), this.host.getFileServerPort(), this.host.getIndexServerPort());
            //all cards are null approach, null state
            the_map.put(item, tempCard);
        }
    }

    private void register_with_null(String filename, ConcurrentHashMap<String, FileBusinessCard> the_map) {
        FileBusinessCard tempCard = new FileBusinessCard(filename, this.host.getPeerID(), this.host.getPeerHost(), this.host.getFileServerPort(), this.host.getIndexServerPort());
        the_map.put(filename, tempCard);
    }

    //
    //VersionMonitor Methods:
    //
    public void setPush(String filename) {
        if (!Push_file_map.containsKey(filename)) {
            //user input error
            System.out.println(filename + " not found in folder \"shared\". Nothing to be done.");
        } else {
            //setup approach for this original file
            FileBusinessCard temp = Push_file_map.get(filename);
            temp.set_approach(FileBusinessCard.Approach.PUSH);
            temp.set_state(FileBusinessCard.State.ORIGINAL);

            Push_file_map.replace(filename, temp);
        }
    }
    //switch off push approach for filename

    public void setPushStop(String filename) {
        if (!Push_file_map.containsKey(filename)) {
            //user input error
            System.out.println(filename + " not found in folder \"shared\". Nothing to be done.");
        } else {
            //setup approach for this original file
            FileBusinessCard temp = Push_file_map.get(filename);
            temp.set_approach(FileBusinessCard.Approach.NULL);
            temp.set_state(FileBusinessCard.State.ORIGINAL);

            Push_file_map.replace(filename, temp);
        }
    }

    public void setPull(String filename, ConcurrentHashMap<String, FileBusinessCard> the_map) {
        if (!the_map.containsKey(filename)) {
            //user input error
            System.out.println(filename + " not found in folder \"shared\". Nothing to be done.");
        } else {
            //setup approach for this copy
            FileBusinessCard temp = the_map.get(filename);
            temp.set_approach(FileBusinessCard.Approach.PULL);
            temp.set_state(FileBusinessCard.State.VALID);
            temp.setTTR(20);
            if (the_map == Push_file_map) {
                temp.set_state(FileBusinessCard.State.ORIGINAL);
            }

            the_map.replace(filename, temp);
        }
    }

    public void setPullStop(String filename, ConcurrentHashMap<String, FileBusinessCard> the_map) {
        if (!the_map.containsKey(filename)) {
            //user input error
            System.out.println(filename + " not found in folder \"shared\". Nothing to be done.");
        } else {
            //setup approach for this copy
            FileBusinessCard temp = the_map.get(filename);
            temp.set_approach(FileBusinessCard.Approach.NULL);
            temp.set_state(FileBusinessCard.State.NULL);

            the_map.replace(filename, temp);
        }
    }

    public void setPullTTR(String filename, ConcurrentHashMap<String, FileBusinessCard> the_map, int TTR) {
        if (!the_map.containsKey(filename)) {
            //user input error
            System.out.println(filename + " not found in folder \"shared\". Nothing to be done.");
        } else {
            //setup approach for this copy
            FileBusinessCard temp = the_map.get(filename);
            temp.setTTR(TTR);

            the_map.replace(filename, temp);
        }

    }

    public FileBusinessCard getACard(String filename, ConcurrentHashMap<String, FileBusinessCard> the_map) {
        return the_map.get(filename);
    }

    public FileBusinessCard getACard(String filename) {
        //Potential ERROR:
        //same file name under both folder
        int i = 0;
        if (this.Push_file_map.containsKey(filename)) {
            i = 1;
        }
        if (this.p2p_file_map.containsKey(filename)) {
            i = 2;
        }
        switch (i) {
            case 0:
                return null;
            case 1:
                return this.Push_file_map.get(filename);
            case 2:
                return this.p2p_file_map.get(filename);
            default:
                return null;
        }
    }

    public void justInvalidate(String filename) {
        if (!p2p_file_map.containsKey(filename)) {
            //error
            System.out.println(filename + " not found in folder \"received\". Nothing to be done.");
        } else {
            //invalidate for the copy
            FileBusinessCard temp = p2p_file_map.get(filename);
            if (temp.get_approach() == FileBusinessCard.Approach.PUSH) {
                temp.set_state(FileBusinessCard.State.INVALID);
            }
            p2p_file_map.replace(filename, temp);
            System.out.println(filename + " is invalidated !");
        }
    }

    private class PushFileListener implements FileListener {

        @Override
        public void fileCreated(FileChangeEvent fce) throws Exception {
            //after init, new file is copied to 'shared' folder
            FileObject temp = fce.getFile();
            System.out.println(" *** \"" + temp.getName().getBaseName() + "\" created in shared folder, ready for push! ***");
            //register the file into the Push_file_table, and set to NULL;
            register_with_null(temp.getName().getBaseName(), Push_file_map);

        }

        @Override
        public void fileDeleted(FileChangeEvent fce) throws Exception {
            //after init, a file is deleted from 'shared' folder
            FileObject temp = fce.getFile();
            System.out.println(" *** \"" + temp.getName().getBaseName() + "\" deleted in shared folder, ready for push! ***/nPUSH WARNING: Original copy shall not be deleted!!");

            Push_file_map.remove(temp.getName().getBaseName());

        }

        @Override
        public void fileChanged(FileChangeEvent fce) throws Exception {
            //todo
            FileObject temp = fce.getFile();
            System.out.println("PUSH TEST: file changed, the version will be increased." + temp.getName().getBaseName());
            //#-1 check approach
            FileBusinessCard targetCard = Push_file_map.get(temp.getName().getBaseName());

            //#-2 version update
            targetCard.increase_versionNumber();
            //#-3 broadcast
            if (targetCard.get_approach() == FileBusinessCard.Approach.PUSH) {
                Push_broadcast_external.offer(temp.getName().getBaseName());
            }



        }
    }

    private class PullFileListener implements FileListener {

        @Override
        public void fileCreated(FileChangeEvent fce) throws Exception {
            FileObject temp = fce.getFile();
            if (p2p_file_map.containsKey(temp.getName().getBaseName())) {
                System.out.println("received folder:" + temp.getName().getBaseName() + " Local Copy Created !");
            } else {
                System.out.println("Please don't just drag a file into \"received\" folder !");
            }

        }

        @Override
        public void fileDeleted(FileChangeEvent fce) throws Exception {
            //todo
            FileObject temp = fce.getFile();
            if (p2p_file_map.containsKey(temp.getName().getBaseName())) {
                System.out.println("received folder:" + temp.getName().getBaseName() + " Local Copy Deleted !");
                p2p_file_map.remove(temp.getName().getBaseName());
            } else {
                System.out.println("A random file in \"received\" folder deleted !");
            }
        }

        @Override
        public void fileChanged(FileChangeEvent fce) throws Exception {
            //todo
            System.out.println("received folder: WARNING Local Copy shall not be modified!");
        }
    }

    //Need a TTR_Timer
    private class TTR_Timer extends Thread {

        boolean enable;

        public TTR_Timer() {
            enable = true;
        }

        @Override
        public void run() {
            while (enable) {
                try {
                    File receivedFolder = new File("received/");
                    String[] files = receivedFolder.list();
                    for (String afile : files) {
                        if (p2p_file_map.containsKey(afile)) {
                            FileBusinessCard theCard = p2p_file_map.get(afile);
                            if (theCard.get_approach() == FileBusinessCard.Approach.PULL) {

                                //TTL not expire, increase TTR
                                if (!theCard.check_TTR_expire()) {
                                    theCard.increase_TTR();
                                    p2p_file_map.replace(afile, theCard);
                                } else if (theCard.check_TTR_expire() && theCard.get_state() == FileBusinessCard.State.VALID) {
                                    //TTL expired AND state is VALID, set TTR_expire, send a job to external deque
                                    theCard.set_state(FileBusinessCard.State.TTR_EXPIRED);
                                    Pull_poll_external.offer(afile);
                                    System.out.println(theCard.get_filename()+" is TTR expired !");
                                }
                            }
                        }
                    }
                    //Timer ticks
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(VersionMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    public static void main(String[] args) {
        System.out.println("Start: main HELLO");
        PeerAddress starter = new PeerAddress(101, "localhost", 5701, 5702);
        VersionMonitor fmr = new VersionMonitor(starter);
        fmr.start();

        while (true) {
            while (!fmr.Push_broadcast_external.isEmpty()) {
                String toBroadcast = fmr.Push_broadcast_external.poll();
                System.out.println(toBroadcast + " need to broadcast !");
            }
            Scanner tester = new Scanner(System.in);
            System.out.println("PUSH a file?");
            String userinput = tester.next();
            fmr.setPush(userinput);
        }

    }
}
