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

import mytorrent.gui.BannerManager;
import mytorrent.gui.CommandParser;

/**
 *
 * @author Bo Feng
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



        //while(true);
        while (true) {
            BannerManager.printCursor();


            CommandParser i = new CommandParser();
            String[] userinput = i.run();
            if (userinput[0].toLowerCase().startsWith("reg")) {
                /* Register */
                System.out.println("This is the Register part!");
                System.out.println(i.Counter());
                System.out.println(userinput[2]);
                System.out.println(userinput[3]);
                continue;
            } else if (userinput[0].toLowerCase().startsWith("sea")) {
                /* Search */
                System.out.println("This is the Search part!");
                continue;
            } else if (userinput[0].toLowerCase().startsWith("obt")) {
                /* Obtain */
                System.out.println("This is the obtain part!");
                continue;

            } else {
                System.out.println("Command not recognized!");
                continue;
            }

        }

    }
}
