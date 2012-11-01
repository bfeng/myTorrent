/*
 * The MIT License
 *
 * Copyright 2012 bfeng.
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

import java.io.FileNotFoundException;
import mytorrent.gui.BannerManager;

/**
 *
 * @author Bo Feng
 * @author Sizhou Wang
 * @version 2.0
 */
public class MyTorrent {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        //#
        //Startup peer, which contains the fileserver thread and indexserver thread
        Peer mainPeer = new Peer();
        mainPeer.startup();
        //#
        //printbanners
        BannerManager.Banner();
        BannerManager.printClientInstruction();
        int MessageID = 1;
        //#
        //Start accepting userinputs
        boolean Running = true;
        while (Running) {
            BannerManager.printCursor(mainPeer.getPeerId());
            String[] userinput = new mytorrent.gui.CommandParser().run();
            if (userinput[0].toLowerCase().equals("que")) {
                /* Query */

                if (userinput.length < 3) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                mainPeer.query2(userinput[1], MessageID, Integer.valueOf(userinput[2]), false);
                MessageID++;

            } else if (userinput[0].toLowerCase().equals("obt")) {
                /* Obtain */
                if (userinput.length < 3) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                mainPeer.obtain2(userinput[1], MessageID, Integer.valueOf(userinput[2]));
                MessageID++;

            } else if (userinput[0].toLowerCase().equals("exit")) {
                //Turn off peer
                System.out.println("  - Client Terminated ! -");
                mainPeer.exit();
                //Turn off CommandParser: not neccessary here.
                //Exit while and end main()
                //Enter here only if > Exit while and end main()
                Running = false;

            } else if (userinput[0].toLowerCase().equals("queryall")) {
                /* Project 2 Query */

                if (userinput.length < 3) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                mainPeer.query(userinput[1], MessageID, Integer.valueOf(userinput[2]), false);
                MessageID++;

            } else if (userinput[0].toLowerCase().equals("obtainall")) {
                /* Project 2 Obtain */
                if (userinput.length < 3) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                mainPeer.obtain2(userinput[1], MessageID, Integer.valueOf(userinput[2]));
                MessageID++;

            } else if (userinput[0].toLowerCase().equals("push")) {
                if (userinput.length != 2) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                String thefile = userinput[2];
                if (mainPeer.indexServer.versionMonitor.Push_file_map.contains(thefile)) {
                    mainPeer.indexServer.versionMonitor.setPush(thefile);
                } else {
                    System.out.println("Wrong filename");
                }

            } else if (userinput[0].toLowerCase().equals("pushstop")) {
                if (userinput.length != 2) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                String thefile = userinput[2];
                if (mainPeer.indexServer.versionMonitor.Push_file_map.contains(thefile)) {
                    mainPeer.indexServer.versionMonitor.setPushStop(thefile);
                } else {
                    System.out.println("Wrong filename");
                }
            } else if (userinput[0].toLowerCase().equals("pull")) {
                if (userinput.length != 2) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                String thefile = userinput[2];
                if (mainPeer.indexServer.versionMonitor.p2p_file_map.contains(thefile)) {
                    System.out.println("You are not original copy owner of the file. You have enabled polling!");
                    mainPeer.indexServer.versionMonitor.setPull(thefile, mainPeer.indexServer.versionMonitor.p2p_file_map);
                } else if (mainPeer.indexServer.versionMonitor.Push_file_map.contains(thefile)) {
                    mainPeer.indexServer.versionMonitor.setPull(thefile, mainPeer.indexServer.versionMonitor.Push_file_map);
                } else {
                    System.out.println("Wrong filename");
                }
            } else if (userinput[0].toLowerCase().equals("pullstop")) {
                if (userinput.length != 2) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                String thefile = userinput[2];
                if (mainPeer.indexServer.versionMonitor.p2p_file_map.contains(thefile)) {
                    System.out.println("You are not original copy owner of the file. You have disabled polling!");
                    mainPeer.indexServer.versionMonitor.setPull(thefile, mainPeer.indexServer.versionMonitor.p2p_file_map);
                } else if (mainPeer.indexServer.versionMonitor.Push_file_map.contains(thefile)) {
                    mainPeer.indexServer.versionMonitor.setPull(thefile, mainPeer.indexServer.versionMonitor.Push_file_map);
                } else {
                    System.out.println("Wrong filename");
                }
            } else if (userinput[0].toLowerCase().equals("pullset")) {
                if (userinput.length != 3) {
                    System.out.println("Wrong parameters");
                    continue;
                }
                String thefile = userinput[2];
                int TTR = Integer.valueOf(userinput[2]);
                if (mainPeer.indexServer.versionMonitor.p2p_file_map.contains(thefile)) {
                    System.out.println("You are not original copy owner of the file. You CANNOT change TTR!");
                } else if (mainPeer.indexServer.versionMonitor.Push_file_map.contains(thefile)) {
                    mainPeer.indexServer.versionMonitor.setPullTTR(thefile, mainPeer.indexServer.versionMonitor.Push_file_map, TTR);
                } else {
                    System.out.println("Wrong filename");
                }
            } else if (userinput[0].toLowerCase().equals("test")) {
                if (userinput.length < 5) {
                    System.err.println("Wrong parameters");

                    System.err.println("Usage: test query|obtian filename TTL repeat");

                    continue;
                }
                int tests = 3;
                if (userinput[1].equalsIgnoreCase("query")) {
                    int repeat = Integer.parseInt(userinput[4]);

                    long[] time = new long[tests];
                    for (int j = 0; j < tests; j++) {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < repeat; i++) {
                            mainPeer.query(userinput[2], MessageID, Integer.parseInt(userinput[3]), true);
                        }
                        long end = System.currentTimeMillis();
                        time[j] = end - start;
                    }
                    System.out.printf("Query: %d, %d, %d\n", time[0], time[1], time[2]);
                } else if (userinput[1].equalsIgnoreCase("obtain")) {
                    int repeat = Integer.parseInt(userinput[4]);

                    long[] time = new long[tests];
                    for (int j = 0; j < tests; j++) {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < repeat; i++) {
                            mainPeer.obtain(userinput[2], MessageID, Integer.parseInt(userinput[3]));
                        }
                        long end = System.currentTimeMillis();
                        time[j] = end - start;
                    }
                    System.out.printf("Query: %d, %d, %d\n", time[0], time[1], time[2]);
                }
            } else if (userinput[0].toLowerCase().equals("help")) {
                BannerManager.printHelp();

            } else {
                System.out.println("Command not recognized!");

            }

        }


    }
}
