package mytorrent.p2p;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        ERR,
        OK
    }

    public class Message {

        protected Command cmd;
        protected Object body;

        public Message() {
        }

        private Message(Command cmd, Object body) {
            this.cmd = cmd;
            this.body = body;
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
}
