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
package mytorrent;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author bfeng
 */
public class PerformanceTest {

    private static Peer peer;

    public PerformanceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        peer = new Peer(5711);

        peer.startup();
    }

    @AfterClass
    public static void tearDownClass() {
        peer.exit();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void test_REG() {
        try {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (int i = 0; i < 1000; i++) {
                long start = System.nanoTime();

                peer.registry(peer.getPeerId(), peer.getSharedFiles());

                long end = System.nanoTime();

                System.out.println((i + 1) + "," + (end - start));
            }
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void test_SCH() {
        try {
            String[] files = {"test1.txt"};
            peer.registry(peer.getPeerId(), files);

            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (int i = 0; i < 1000; i++) {
                long start = System.nanoTime();

                peer.search("test1.txt");

                long end = System.nanoTime();

                System.out.println((i + 1) + "," + (end - start));
            }
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void test_multiclient_REG() {
        try {
            Peer[] peers = new Peer[9];

            for (int n = 0; n < peers.length; n++) {
                peers[n] = new Peer(5712 + n);
                peers[n].startup();
            }

            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (int i = 0; i < 100; i++) {
                long start = System.nanoTime();

                peer.registry(peer.getPeerId(), peer.getSharedFiles());

                long end = System.nanoTime();

                System.out.println("Peer 0," + (i + 1) + "," + (end - start));
            }

            for (int n = 0; n < peers.length; n++) {
                Peer p = peers[n];
                for (int i = 0; i < 100; i++) {
                    long start = System.nanoTime();

                    p.registry(p.getPeerId(), p.getSharedFiles());

                    long end = System.nanoTime();

                    System.out.println("Peer " + (n + 1) + "," + (i + 1) + "," + (end - start));
                }
            }
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            for (Peer p : peers) {
                p.exit();
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void test_multiclient_SCH() {
        try {
            String[] files = {"test1.txt"};
            peer.registry(peer.getPeerId(), files);
            
            Peer[] peers = new Peer[9];

            for (int n = 0; n < peers.length; n++) {
                peers[n] = new Peer(5712 + n);
                peers[n].startup();
            }

            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (int i = 0; i < 100; i++) {
                long start = System.nanoTime();

                peer.search("test1.txt");

                long end = System.nanoTime();

                System.out.println("Peer 0," + (i + 1) + "," + (end - start));
            }

            for (int n = 0; n < peers.length; n++) {
                Peer p = peers[n];
                for (int i = 0; i < 100; i++) {
                    long start = System.nanoTime();

                    p.search("test1.txt");

                    long end = System.nanoTime();

                    System.out.println("Peer " + (n + 1) + "," + (i + 1) + "," + (end - start));
                }
            }
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            for (Peer p : peers) {
                p.exit();
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
