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
package mytorrent.p2p;

/**
 *
 * @author Bo Feng
 * @version 2.0
 */
public class PeerAddress {

    private int peerID;
    private String peerHost;
    private int fileServerPort;
    private int indexServerPort;

    public PeerAddress() {
    }

    public PeerAddress(int peerID, String peerHost, int indexServerPort, int fileServerPort) {
        this.peerID = peerID;
        this.peerHost = peerHost;
        this.fileServerPort = fileServerPort;
        this.indexServerPort = indexServerPort;
    }

    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID) {
        this.peerID = peerID;
    }

    public String getPeerHost() {
        return peerHost;
    }

    public void setPeerHost(String peerHost) {
        this.peerHost = peerHost;
    }

    public int getFileServerPort() {
        return fileServerPort;
    }

    public void setFileServerPort(int fileServerPort) {
        this.fileServerPort = fileServerPort;
    }

    public int getIndexServerPort() {
        return indexServerPort;
    }

    public void setIndexServerPort(int indexServerPort) {
        this.indexServerPort = indexServerPort;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PeerAddress other = (PeerAddress) obj;
        if (this.peerID != other.peerID) {
            return false;
        }
        if ((this.peerHost == null) ? (other.peerHost != null) : !this.peerHost.equals(other.peerHost)) {
            return false;
        }
        if (this.fileServerPort != other.fileServerPort) {
            return false;
        }
        if (this.indexServerPort != other.indexServerPort) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.peerID;
        hash = 89 * hash + (this.peerHost != null ? this.peerHost.hashCode() : 0);
        hash = 89 * hash + this.fileServerPort;
        hash = 89 * hash + this.indexServerPort;
        return hash;
    }
}
