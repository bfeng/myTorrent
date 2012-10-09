package mytorrent.gui;

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
        System.out.println("*   Welcome to CS550 Project <1>  *");
        System.out.println("*                                 *");
        System.out.println("* Author:      Bo Feng: A20273356 *");
        System.out.println("*          Sizhou Wang: A20249772 *");
        System.out.println("* * * * * * * * * * * * * * * * * *");


    }

    public static void printClientInstruction() {
        System.out.println("User Input Direction:");
        System.out.println(" * Registry: registry");
        System.out.println(" * Search: search filename");
        System.out.println(" * Obtain: obtain filename");
        System.out.println(" * Lookup Peer Address: lookup peerID ");
        System.out.println(" * Help: help (detailed info)");
        System.out.println(" * Exit Client: exit");
    }

    public static void printCursor() {
        System.out.print("myTorrent >>");
    }

    public static void printSearchReturns(FileHash.Entry[] entrytoprint) {
        System.out.println("  - Search Result: " + entrytoprint.length + " found in the network");
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
        System.out.println("[Registry]: Files under \"shared\" folder will be registered onto IndexServer");
        System.out.println("            You can type \"register\" or simply \"reg\" to complete this action.");
        System.out.println("  [Search]: Search and list all peer(with peerID) who have the file called \"filename\" ");
        System.out.println("            You can type \"search filename\" or simply \"sea filename\" to complete this action.");
        System.out.println("  [Obtain]: Specific file will be automaticly downloaded to your \"shared\" folder ");
        System.out.println("            You can type \"obtain filename\" or simply \"obt filename\" to complete this action.");
        System.out.println("  [Lookup]: Lookup a specific peer's IP address and port number with PeerID. PeerID is always returned from search.");
        System.out.println("            You can type \"lookup peerID\" or simply \"look peerID\" to complete this action.");
        System.out.println("    [Exit]: You can exit this client by typing \"exit\"\n");
    }
}
