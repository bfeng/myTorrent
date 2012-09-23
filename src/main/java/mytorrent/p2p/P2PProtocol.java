package mytorrent.p2p;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bo Feng
 * @version 1.0
 */
public class P2PProtocol {

    public enum Command {

        REG,
        SCH,
        LOK,
        ERR,
        PIG,
        OK
    }

    public class Message {

        protected Command cmd;
        protected Object body;

        public Message() {
        }

        public Message(Command cmd, Object body) {
            this.cmd = cmd;
            this.body = body;
        } 

        public Command getCmd() {
            return cmd;
        }

        public void setCmd(Command cmd) {
            this.cmd = cmd;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
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
            return new Message(Command.ERR, ex);
        }
    }

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
}
