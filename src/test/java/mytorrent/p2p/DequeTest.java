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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import mytorrent.p2p.P2PProtocol.HitMessage;
import mytorrent.p2p.P2PProtocol.QueryMessage;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author bfeng
 */
public class DequeTest {

    @Test
    public void test_iterator() {
        Deque<Long> stack = new ArrayDeque<Long>();

        stack.push(1L);
        stack.push(2L);
        stack.push(3L);

        Iterator<Long> iter = stack.iterator();

        assertEquals(3, iter.next().longValue());
        assertEquals(2, iter.next().longValue());
        assertEquals(1, iter.next().longValue());
    }

    @Test
    public void test_debug_path() {
        P2PProtocol p = new P2PProtocol();
        
        QueryMessage qm = p.new QueryMessage(101, -1, 3);
        
        assertEquals("[101]", qm.debugPath());
        
        qm.addPath(102);
        
        assertEquals("[101] -> 102", qm.debugPath());
        
        qm.addPath(103);
        
        assertEquals("[101] -> 102 -> 103", qm.debugPath());
        
        HitMessage hm = p.new HitMessage(qm);
        
        assertEquals("[101] -> 102 -> 103", hm.debugPath());
        
        hm.nextPath();
        
        assertEquals("[101] -> 102", hm.debugPath());
        
        hm.nextPath();
        
        assertEquals("[101]", hm.debugPath());
        
        assertEquals("[101] -> 102 -> 103", qm.debugPath());
    }
}
