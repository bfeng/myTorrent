package mytorrent.p2p;

import java.io.File;

/**
 *
 * @author Bo
 */
public interface P2PClient {

    public File obtain(String filename);
}
