/*
 * 
 */
package mytorrent.gui;

/**
 *
 * @author swang
 * 
 */
import java.util.Scanner;

public class CommandParser /* implements Runnable */ {

    //
    //Field
    //
    private static int Counter = 0;
    private int index;
    private Scanner userScanner;
    private String userInputRaw;
    private String[] userInput;
    public static String[] userHistory = new String[256];

    //
    //Constructor
    //
    //#1
    //(1)Accumulate command counter by 1;
    //(2)Invoke user input and parse the input string into string array;
    public CommandParser() {
        Counter++;
        index = Counter;
    }

    //
    //Methods
    //
    public /* need return type */ String[] run() {

        //get input from user, multiple inputs or single
        //parse the input args
        userScanner = new Scanner(System.in);
        userInputRaw = userScanner.nextLine();
        userInput = userInputRaw.split("\\s");

        //register user input history to static
        if (index <= 256) {
            userHistory[index - 1] = userInputRaw;
        } else {
            System.out.println("History is full, clear history and backup to file.");
            //back up to file
            throw new UnsupportedOperationException("Not supported yet.");
        }
        //return type
        return userInput;
    }

    public static int Counter() {
        return Counter;
    }
}
