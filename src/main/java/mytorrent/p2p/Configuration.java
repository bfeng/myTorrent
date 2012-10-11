/*
 * The MIT License
 *
 * Copyright 2012 Bo.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Bo
 */
public class Configuration {

    private PeerAddress hostAddress;
    private PeerAddress[] neighbors;

    private Configuration() {
    }

    public static Configuration load(String filepath) throws FileNotFoundException {
        Configuration result = new Configuration();
        Reader input = new FileReader(new File(filepath));
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.load(input);
        result.hostAddress = map2PeerAddress(map);
        List<Map> neighborList = (List<Map>) map.get("Neighbors");
        result.neighbors = new PeerAddress[neighborList.size()];
        for (int i = 0; i < neighborList.size(); i++) {
            result.neighbors[i] = map2PeerAddress(neighborList.get(i));
        }
        return result;
    }

    private static PeerAddress map2PeerAddress(Map<String, Object> map) {
        PeerAddress pa = new PeerAddress();
        pa.setPeerID((Integer) map.get("PeerID"));
        pa.setPeerHost((String)map.get("PeerHost"));
        pa.setFileServerPort((Integer) map.get("FileServerPort"));
        pa.setIndexServerPort((Integer) map.get("IndexServerPort"));
        return pa;
    }

    public PeerAddress getHostAddress() {
        return hostAddress;
    }

    public PeerAddress[] getNeighbors() {
        return neighbors;
    }
}
