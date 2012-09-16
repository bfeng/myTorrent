/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mytorrent.gui;

import mytorrent.p2p.Address;
import mytorrent.p2p.FileHash;

/**
 *
 * @author Swang
 */

public class BannerManager {

    //
    //Fields
    //
    private static String dir = System.getProperty("user.dir");

    //private String clearScreenCommand = null;
    //private String [] clearScreenCommandtmp = {"cmd", "/C", "start", dir+"\\ab.bat"};
    //  
    //Methods
    //

    public static void clearConsole() {

        for (int i = 15; i > 0; i--) {
            System.out.println("\n");
        }
    }

    public static void printBanner() {
        System.out.println("Input Direction:");
        System.out.println("(1) Registry: registry ");
        System.out.println("(2) Search: search filename");
        System.out.println("(3) Obtain: in progress ...");

    }

    public static void printCursor() {
        System.out.print("Cursor>>");
    }
    
    public static void printSearchReturns(FileHash.Entry[] entrytoprint) {
        System.out.println("Result: " + entrytoprint.length + " found in the network");
        for (FileHash.Entry item: entrytoprint) {
            System.out.println(item.getFilename() + " at " + item.getPeerId());
        }
    }
    public static void printLookupReturns(Address addresstoprint, long peerId) {
        System.out.println("Host: " + addresstoprint.getHost());
        System.out.println("Port: " + addresstoprint.getPort());
 
    }
    
}
