package mytorrent.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.PeerAddress;
import mytorrent.p2p.FileHash;

/**
 *
 * @author Swang
 */
public class BannerManager {

    //
    //Fields
    //
    //  
    //Methods
    //
    public static void clearConsole() {

        for (int i = 15; i > 0; i--) {
            System.out.println("\n");
        }
    }

    public static void Banner() {
        System.out.println("* * * * * * * * * * * * * * * * * *");
        System.out.println("*   Welcome to CS550 Project <2>  *");
        System.out.println("*                                 *");
        System.out.println("* Author:      Bo Feng: A20273356 *");
        System.out.println("*          Sizhou Wang: A20249772 *");
        System.out.println("* * * * * * * * * * * * * * * * * *");
    }

    public static void printClientInstruction() {
        System.out.println("User Input Direction:");
        System.out.println(" * Query: query filename TTL");
        System.out.println(" * Obtain: obtain filename TTL");
        System.out.println(" * Help: help (detailed info)");
        System.out.println(" * Exit Client: exit");
    }

    public static void printCursor() {
        System.out.print("$ >>");
    }

    public static void printCursor(long peerId) {
        System.out.print("Peer " + peerId + " >");
    }

    public static void printSearchReturns(FileHash.Entry[] entrytoprint) {
        System.out.println("  - Query Result: " + entrytoprint.length + " found in the network");
        for (FileHash.Entry item : entrytoprint) {
            System.out.println("    " + item.getFilename() + " at PeerID: " + item.getPeerId());
        }
    }

    public static void printLookupReturns(PeerAddress addresstoprint, long peerId) {
        System.out.println("    Host: " + addresstoprint.getPeerHost());
        System.out.println("    Port: " + addresstoprint.getFileServerPort());

    }

    public static void printUsage() {
        System.out.println("Please select client usage:");
        System.out.println("1 - Regular use. (You will need to specify IndexServer address, port and Client port.).");
        System.out.println("2 - Test use. (You will only need to specify Client port; IndexServer and client are both on localhost)");
        System.out.print("Your Choice >");
    }

    public static void printHelp() {
        System.out.println("\n Welcome to Help !");
        System.out.println(" [Query]: Search and list all peer who have the file called \"filename\" ");
        System.out.println("          You can type \"search filename\"");
        System.out.println("          or simply \"sea filename\" to complete this action.");
        System.out.println("          TTL is an integer. It specify the wides of the network you want to query.");
        System.out.println("[Obtain]: Specific file will be automaticly downloaded to your \"received\" folder ");
        System.out.println("          You can type \"obtain filename\"");
        System.out.println("          or simply \"obt filename\" to complete this action.");
        System.out.println("  [Help]: See this message by typing \"help\"\n");
        System.out.println("  [Exit]: You can exit this client by typing \"exit\"\n");
    }
    /*
     public static void QueryWaiting() throws InterruptedException {
     System.out.println("|                    Please Wait                   |");
     System.out.print("|");
     for (int i = 0; i < 16; i++) {

     System.out.print("=");
     Thread.sleep(133);
     System.out.print("=");
     Thread.sleep(133);
     System.out.print("=");
     Thread.sleep(133);
     }
     System.out.print("==|\n");



     }
     */

    public static void obtainResult(String filename, int flag) {
        if (flag == 1) {
            System.out.println("\n" + filename + " successfully obtained !\n");

        } else {
            System.out.println("\n" + filename + " not found in the network. Please try again later !\n");
        }
    }
}
