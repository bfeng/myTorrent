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

import com.google.gson.internal.StringMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.gui.BannerManager;
import mytorrent.gui.CommandParser;
import mytorrent.p2p.Address;
import mytorrent.p2p.FileHash;

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

        //define thispeer
        Peer thispeer = null;
        //Print banner
        BannerManager.Banner();


        //Ask user for usage specification
        //1 for regular use: input peer(int clientPort, String serveraddress, int serverport);
        //2 for test use: input peer(int clientport);
        BannerManager.printUsage();

        boolean inusagechoice = true;
        while (inusagechoice) {
            InputStream sysin = System.in;
            Scanner portScanner = new Scanner(sysin);
            String usagechoice = portScanner.nextLine();

            if (usagechoice.equals("1")) {
                inusagechoice = false;
                System.out.print("\nPlease input IndexServer IP Address >");
                String IndexServerAddress = portScanner.nextLine();
                System.out.print("\nPlease input IndexServer port number >");
                int IndexServerPort = Integer.parseInt(portScanner.nextLine());
                System.out.print("\nPlease input this client port number >");
                int thispeerport = Integer.parseInt(portScanner.nextLine());
                //start peer
                thispeer = new Peer(thispeerport, IndexServerAddress, IndexServerPort);
                thispeer.startup();
                System.out.println("Client is Running.");

            } else if (usagechoice.equals("2")) {
                inusagechoice = false;
                //Ask user for peer port"
                System.out.print("\nIndexServer is set to localhost on port 5700");
                System.out.print("\nPlease input this client port number >");
                String portInputRaw = portScanner.nextLine();
                //portScanner.close();
                //create a peer and thispeer.server thread;
                thispeer = new Peer(Integer.parseInt(portInputRaw));
                thispeer.startup();
                System.out.println("\nClient is Running...\n");
            } else {
                System.out.print("\nPlease input 1 or 2.");
            }
        }


        BannerManager.printClientInstruction();
        //while(true);
        boolean Running = true;
        while (Running) {
            BannerManager.printCursor();
            String[] userinput = new CommandParser().run();
            if (userinput[0].toLowerCase().startsWith("reg")) {
                /* Registry */
                System.out.println("  - Register Finished ! -");
                //Userinput: registry
                //MyTorrent input: long thispeerId String[]filesofthis
                //#
                //First prepare inputs:
                //#
                //Second call registry and handle return value
                thispeer.setPeerId(thispeer.registry(thispeer.getPeerId(), thispeer.getSharedFiles()));

            } else if (userinput[0].toLowerCase().startsWith("sea")) {
                /* Search */
                //System.out.println("This is the Search part!");
                //Userinput: search Afilename
                //MyTorrent input: String Afilename
                //#
                //First prepare inputs
                //#
                //Second call search and print return value
                BannerManager.printSearchReturns(thispeer.search(userinput[1]));

            } else if (userinput[0].toLowerCase().startsWith("obt")) {
                /* Obtain */
                System.out.println("  - File Obtained ! -");
                thispeer.obtain(userinput[1]);


            } else if (userinput[0].toLowerCase().startsWith("look")) {
                /* Lookup */
                System.out.println("  - Lookup Result ! -");
                //Userinput: lookup ApeerId
                //MyTorrent input: long ApeerId
                //#
                //First prepare inputs
                //#
                //Second call lookup()
                long lookpeerId = Long.valueOf(userinput[1]);
                Address lookupreturns = thispeer.lookup(lookpeerId);
                BannerManager.printLookupReturns(lookupreturns, lookpeerId);


            } else if (userinput[0].toLowerCase().equals("exit")) {
                //Turn off peer
                System.out.println("  - Client Terminated ! -");
                thispeer.exit();
                //Turn off CommandParser: not neccessary here.
                //Exit while and end main()
                //Enter here only if > Exit while and end main()
                Running = false;
            } else if (userinput[0].toLowerCase().equals("help")) {
                BannerManager.printHelp();
          
            }else {
                System.out.println("Command not recognized!");

            }

        }

    }
}
