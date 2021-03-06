package mytorrent.p2p;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bo Feng
 * @version 2.0
 */
public class P2PProtocol {

    private class AbstractMessage {

        private long peerID;
        private long messageID;
        private String filename;
        private Deque<Long> stack; // a stack to storage all nodes in the path

        public AbstractMessage(long peerId, long messageId) {
            this.peerID = peerId;
            this.messageID = messageId;
            this.stack = new LinkedList<Long>();
            this.filename = null;
        }

        public long getPeerID() {
            return peerID;
        }

        public void setPeerID(long peerID) {
            this.peerID = peerID;
        }

        public long getMessageID() {
            return messageID;
        }

        public void setMessageID(long messageID) {
            this.messageID = messageID;
        }

        protected Long nextPath() {
            return stack.pop();
        }

        protected void addPath(long peerID) {
            stack.push(peerID);
        }

        protected boolean searchPath(long peerId) {
            return stack.contains(peerId);
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return this.filename;
        }

        public int size() {
            return stack.size();
        }

        public String debugPath() {
            StringBuilder sb = new StringBuilder("[");
            sb.append(peerID);
            sb.append("]");
            if (!stack.isEmpty()) {
                sb.append(" -> ");
                for (Iterator<Long> iter = stack.descendingIterator(); iter.hasNext();) {
                    sb.append(iter.next());
                    if (iter.hasNext()) {
                        sb.append(" -> ");
                    }
                }
            }
            return sb.toString();
        }
    }

    public class QueryMessage {

        private AbstractMessage message;
        private int TTL;

        public QueryMessage(long peerId, long messageId, int TTL) {
            this.message = new AbstractMessage(peerId, messageId);
            this.TTL = TTL;
        }

        public synchronized boolean isLive() {
            return TTL > 0;
        }

        public synchronized void addPath(long peerId) {
            this.message.addPath(peerId);
        }

        public synchronized boolean searchPath(long peerId) {
            return this.message.searchPath(peerId);
        }

        public synchronized void decrementTTL() {
            TTL--;
        }

        public long getPeerID() {
            return this.message.getPeerID();
        }

        public long getMessageID() {
            return this.message.getMessageID();
        }

        public HitMessage reply() {
            throw new UnsupportedOperationException();
        }

        public String getFilename() {
            return this.message.getFilename();
        }

        public void setFilename(String filename) {
            this.message.setFilename(filename);
        }

        public int size() {
            return message.size();
        }

        public String debugPath() {
            return this.message.debugPath();
        }
    }

    public class HitMessage {

        private AbstractMessage message;
        private Result result;
        private HitAddress hitAddress;

        public HitMessage(QueryMessage qm) {
            this.message = new AbstractMessage(qm.getPeerID(), qm.getMessageID());
            this.message.filename = qm.getFilename();
            this.message.stack = (Deque<Long>) ((LinkedList<Long>) qm.message.stack).clone();
        }

        public synchronized long nextPath() {
            long peerId;
            try {
                peerId = this.message.nextPath();
            } catch (java.util.NoSuchElementException e) {
                peerId = this.message.getPeerID();
            }
            return peerId;
        }

        public long getPeerID() {
            return this.message.getPeerID();
        }

        public long getMessageID() {
            return this.message.getMessageID();
        }

        public void miss() {
            this.result = Result.MISS;
        }

        public void hit(long peerID, String host, int port, int ISport) {
            this.result = Result.HIT;
            this.hitAddress = new HitMessage.HitAddress(peerID, host, port, ISport);
        }

        public Result getResult() {
            return this.result;
        }

        public String getHitPeerHost() {
            if (this.result == Result.HIT) {
                return this.hitAddress.peerHost;
            }
            return null;
        }

        public long getHitPeerID() {
            if (this.result == Result.HIT) {
                return this.hitAddress.peerID;
            }
            return -1;
        }

        public int getHitPeerISPort() {
            if (this.result == Result.HIT) {
                return this.hitAddress.peerISPort;
            }
            return -1;
        }

        public int getHitPeerFSPort() {
            if (this.result == Result.HIT) {
                return this.hitAddress.peerFSPort;
            }
            return 0;
        }

        public void setFilename(String filename) {
            this.message.setFilename(filename);
        }

        public String getFilename() {
            return this.message.getFilename();
        }

        private final class HitAddress {

            public String peerHost;
            public int peerFSPort;
            public int peerISPort;
            public long peerID;

            private HitAddress(long peerID, String host, int port, int ISport) {
                this.peerHost = host;
                this.peerFSPort = port;
                this.peerID = peerID;
                this.peerISPort = ISport;
            }
        }

        public int size() {
            return message.size();
        }

        public String debugPath() {
            return this.message.debugPath();
        }
    }

    public enum Result {

        MISS, HIT
    }

    public enum Command {

        ERR,
        ING,
        PING,
        PONG,
        QUERYMSG,
        HITMSG,
        INVALIDATE,
        QUERYMSG2, //in order to preserve original query function, this query only returns valid copies
        CARD, //the msg ask an indexServer to return a specific FileBusinessCard
        OK //multifunction message return
    }

    public class Message {

        private Command cmd;
        private QueryMessage queryMessage;
        private HitMessage hitMessage;
        private FileBusinessCard fileBusinessCard;

        public Message(Command cmd) {
            this.cmd = cmd;
        }

        public Message(QueryMessage queryMessage) {
            this.queryMessage = queryMessage;
            this.cmd = Command.QUERYMSG;
        }

        public Message(HitMessage hitMessage) {
            this.hitMessage = hitMessage;
            this.cmd = Command.HITMSG;
        }

        public Message(FileBusinessCard fileBusinessCard) {
            this.fileBusinessCard = fileBusinessCard;
        }

        public Command getCmd() {
            return cmd;
        }

        public void setCmd(Command cmd) {
            this.cmd = cmd;
        }

        public QueryMessage getQueryMessage() {
            return queryMessage;
        }

        public void setQueryMessage(QueryMessage queryMessage) {
            this.queryMessage = queryMessage;
        }

        public HitMessage getHitMessage() {
            if (this.cmd == Command.HITMSG) {
                return hitMessage;
            } else {
                return null;
            }
        }

        public void setHitMessage(HitMessage hitMessage) {
            this.hitMessage = hitMessage;
        }

        public void setFileBusinessCard(FileBusinessCard fileBusinessCard) {
            this.fileBusinessCard = fileBusinessCard;
        }

        public FileBusinessCard getFileBusinessCard() {
            return this.fileBusinessCard;
        }

        @Override
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this, Message.class);
        }
    }

    public Message processInput(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            Gson gson = new Gson();
            return gson.fromJson(br, Message.class);
        } catch (Exception ex) {
            Logger.getLogger(P2PProtocol.class.getName()).log(Level.SEVERE, null, ex);
            return new Message(Command.ERR);
        }
    }

    /**
     * This method could be used as a intermediate output method, whose output
     * should be closed finally.
     *
     * @param os
     * @param src
     */
    public void preparedOutput(OutputStream os, Message src) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            Gson gson = new Gson();
            gson.toJson(src, bw);
            bw.flush();
        } catch (Exception ex) {
            Logger.getLogger(P2PProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void processOutput(OutputStream os, Message src) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
            Gson gson = new Gson();
            gson.toJson(src, bw);
            bw.flush();
            os.close();
        } catch (Exception ex) {
            Logger.getLogger(P2PProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
