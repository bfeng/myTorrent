/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mytorrent.gui;

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
    /*
    public void clearConsole() {
    
    
    
    if(System.getProperty("os.name").startsWith("Window"))
    {
    
    System.out.println(dir);
    
    //clearScreenCommand = "cmd.exe /C start " + dir + "\\ab.bat";
    
    System.out.println("cls is choosen.");
    }
    
    else
    clearScreenCommand = "clear";
    try {
    Runtime.getRuntime().exec(clearScreenCommandtmp);
    
    // Graphics dg = drawingArea.getGraphics();
    // Rectangle r = drawingArea.bounds();
    // dg.setColor (this.getBackground ());
    // dg.fillRect (r.x, r.y, r.width, r.height);
    
    
    } catch (IOException ex) {
    Logger.getLogger(BannerManager.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    }
     */
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
}
