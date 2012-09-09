package mytorrent;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import mytorrent.p2p.FileHash;
import mytorrent.p2p.P2PClient;
import mytorrent.p2p.P2PSender;
import mytorrent.p2p.P2PTransfer;

/**
 *
 * @author Bo
 */
public class Peer implements P2PTransfer, P2PClient {

    private final int port;
    private final Server server;

    public Peer(int port) {
        this.port = port;
        this.server = new Server(this.port);
        this.server.setDaemon(true);
    }

    @Override
    public long registry(int peerId, String[] files) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileHash.Entry[] search(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File obtain(String filename) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startup() {
        this.server.start();
    }

    @Override
    public void exit() {
        this.server.close();
    }

    @Override
    public boolean ping() {
        return this.server.isAlive() && this.server.isDaemon();
    }

    public class Server extends Thread {

        private final int port;
        private boolean running;
        private ServerSocket listener;

        private Server(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                this.running = true;
                listener = new ServerSocket(port);
                while (running) {
                    Socket sock = listener.accept();
                    P2PSender sender = new P2PSender(sock);
                    sender.start();
                }
            } catch (IOException ex) {
                Logger.getLogger(MyTorrent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void close() {
            this.running = false;
            if (this.listener != null) {
                try {
                    this.listener.close();
                } catch (IOException ex) {
                    Logger.getLogger(Peer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
