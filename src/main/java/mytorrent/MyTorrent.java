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

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.gui.BannerManager;
import mytorrent.gui.CommandParser;

/**
 *
 * @author Bo Feng
 * @author Sizhou Wang
 * @version 1.0
 */
public class MyTorrent {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Print banner
        /* BannerManager.clearConsole();*/
        BannerManager.printBanner();

        //Ask user for peer port"
        InputStream sysin = System.in;
        System.out.print("\nPlease input port number >>>");
        Scanner portScanner = new Scanner(sysin);
        String portInputRaw = portScanner.nextLine();
        //portScanner.close();
          
         
        
        //create a peer and thispeer.server thread;
        Peer thispeer = new Peer(Integer.parseInt(portInputRaw));
        thispeer.startup();


        //while(true);
        boolean Running = true;
        while (Running) {
            BannerManager.printCursor();


            CommandParser i = new CommandParser();
            String[] userinput = i.run();
            if (userinput[0].toLowerCase().startsWith("reg")) {
                /* Register */
                System.out.println("This is the Register part!");
                continue;
            } else if (userinput[0].toLowerCase().startsWith("sea")) {
                /* Search */
                System.out.println("This is the Search part!");
                continue;
            } else if (userinput[0].toLowerCase().startsWith("obt")) {
                /* Obtain */
                System.out.println("This is the obtain part!");
                continue;

            } else if (userinput[0].toLowerCase().equals("exit"))  {
                //Turn off peer
                thispeer.exit();
                //Turn off CommandParser: not neccessary here.
                //Exit while and end main()
            } else {
                System.out.println("Command not recognized!");
                continue;
            }

            //Enter here only if > Exit while and end main()
            Running = false;
        }

    }
}
