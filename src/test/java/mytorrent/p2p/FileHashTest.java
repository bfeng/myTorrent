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

import mytorrent.p2p.FileHash.Entry;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Bo Feng
 */
public class FileHashTest {

    public FileHashTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addEntry and getEntry method, of class FileHash.
     */
    @Test
    public void testAddGetEntry() {
        System.out.println("addEntry");
        FileHash instance = new FileHash();
        Entry entry = instance.new Entry(100, "test");
        instance.addEntry(entry);

        Entry result = instance.getEntry(100, "test");
        assertEquals(entry, result);
    }

    /**
     * Test of search method, of class FileHash.
     */
    @Test
    public void testSearch_0args() {
        System.out.println("search");
        FileHash instance = new FileHash();
        Entry entry1 = instance.new Entry(100, "test1");
        Entry entry2 = instance.new Entry(100, "test2");
        Entry entry3 = instance.new Entry(200, "test1");
        instance.addEntry(entry1);
        instance.addEntry(entry2);
        instance.addEntry(entry3);
        Entry[] expResult = {entry1, entry2, entry3};
        Entry[] result = instance.search();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of search method, of class FileHash.
     */
    @Test
    public void testSearch_int() {
        System.out.println("search");
        FileHash instance = new FileHash();
        Entry entry1 = instance.new Entry(100, "test1");
        Entry entry2 = instance.new Entry(100, "test2");
        Entry entry3 = instance.new Entry(200, "test1");
        instance.addEntry(entry1);
        instance.addEntry(entry2);
        instance.addEntry(entry3);
        Entry[] expResult = {entry1, entry2};
        Entry[] result = instance.search(100);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of search method, of class FileHash.
     */
    @Test
    public void testSearch_String() {
        System.out.println("search");
        FileHash instance = new FileHash();
        Entry entry1 = instance.new Entry(100, "test1");
        Entry entry2 = instance.new Entry(100, "test2");
        Entry entry3 = instance.new Entry(200, "test1");
        instance.addEntry(entry1);
        instance.addEntry(entry2);
        instance.addEntry(entry3);
        Entry[] expResult = {entry1, entry3};
        Entry[] result = instance.search("test1");
        assertArrayEquals(expResult, result);
    }
}
