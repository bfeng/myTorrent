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
        //#
        //Start accepting userinputs
        boolean Running = true;
        while (Running) {
            BannerManager.printCursor();
            String[] userinput = new mytorrent.gui.CommandParser().run();
            if (userinput[0].toLowerCase().startsWith("que")) {
                /* Query */
                //Userinput: userinput[1]
                //MyTorrent input: long thispeerId String[]filesofthis
                //#
                //First prepare inputs
                //#
                //Second call query and handle return value
                

            } else if (userinput[0].toLowerCase().startsWith("obt")) {
                /* Obtain */




            } else if (userinput[0].toLowerCase().equals("exit")) {
                //Turn off peer
                System.out.println("  - Client Terminated ! -");
                mainPeer.exit();
                //Turn off CommandParser: not neccessary here.
                //Exit while and end main()
                //Enter here only if > Exit while and end main()
                Running = false;
            } else if (userinput[0].toLowerCase().equals("help")) {
                BannerManager.printHelp();

            } else {
                System.out.println("Command not recognized!");

            }

        }


    }
}
