/*
 * The MIT License
 *
 * Copyright 2012 owner.
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
package mytorrent.p2p;

/**
 *
 * @author swang
 */
public class FileBusinessCard {

    //info: original copy ownner, no matter PULL or PUSH
    String filename;
    long peerID;
    String peerHost;
    int fileServerPort;
    int indexServerPort;
    //changable
    Approach approach;
    State state;
    int versionNumber;
    int TTR_threshold;
    int TTR; //in an increasement manner

    public enum Approach {

        NULL,
        PUSH,
        PULL
    }

    public enum State {

        NULL,
        ORIGINAL,
        VALID,
        INVALID,
        TTR_EXPIRED
    }

    public FileBusinessCard() {
        filename = "null";
    }

    public FileBusinessCard(String filenameIN, long peerIDIN, String peerHostIN, int fileServerPortIN, int indexServerPortIN) {

        filename = filenameIN;
        peerID = peerIDIN;
        peerHost = peerHostIN;
        fileServerPort = fileServerPortIN;
        indexServerPort = indexServerPortIN;

        approach = Approach.NULL;
        state = State.NULL;
        versionNumber = 0;
        TTR_threshold = -1;
        TTR = 0;
    }

    //get_info
    public String get_filename() {
        return filename;
    }

    public long get_peerID() {
        return peerID;
    }

    public String get_peerHost() {
        return peerHost;
    }

    public int get_fileServerPort() {
        return fileServerPort;
    }

    public int get_indexServerPort() {
        return indexServerPort;
    }

    //get_changable
    public Approach get_approach() {
        return approach;
    }

    public State get_state() {
        return state;
    }

    public int get_versionNumber() {
        return versionNumber;
    }

    //set_changable
    public void set_approach(Approach approachIN) {
        this.approach = approachIN;
    }

    public void set_state(State stateIN) {
        this.state = stateIN;
    }

    public void set_versionNumber(int versionNumberIN) {
        this.versionNumber = versionNumberIN;
    }
    
    public void increase_versionNumber() {
        this.versionNumber++;
    }
    
    public void increase_TTR() {
        if(this.TTR < this.TTR_threshold)
            this.TTR++;
    }
    
    public boolean check_TTR_expire() {
        if(this.TTR >= this.TTR_threshold)
            return true;
        else
            return false;
    }
    
    public void setTTR(int ttr) {
        this.TTR_threshold = ttr;
    }
    
    public void setTTRvalue(int ttr) {
        this.TTR = ttr;
    }
}
