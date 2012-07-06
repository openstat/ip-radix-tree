/*
 * Copyright (C) 2012 Openstat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openstat;

import static org.testng.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.testng.annotations.Test;

@Test
public class RadixTreeTests {
    @Test
    public void testCidrInclusion() {
        IPv4RadixIntTree tr = new IPv4RadixIntTree(100);
        tr.put(0x0a000000, 0xffffff00, 42);
        tr.put(0x0a000000, 0xff000000, 69);

        assertEquals(tr.selectValue(0x0a202020), 69);
        assertEquals(tr.selectValue(0x0a000020), 42);
        assertEquals(tr.selectValue(0x0b010203), IPv4RadixIntTree.NO_VALUE);
    }

    @Test
    public void testRealistic() throws IOException {
        IPv4RadixIntTree tr = IPv4RadixIntTree.loadFromLocalFile("test/ip-prefix-base.txt");
        BufferedReader br = new BufferedReader(new FileReader("test/test-1.txt"));
        String l;
        int n = 0;
        while ((l = br.readLine()) != null) {
            String[] c = l.split("\t", -1);
            assertEquals(tr.selectValue(c[0]), Integer.parseInt(c[1]), "Mismatch in line #" + n);
            n++;
        }
        System.out.println(tr.size());
    }

    @Test
    public void testNginx() throws IOException {
        IPv4RadixIntTree tr = IPv4RadixIntTree.loadFromLocalFile("test/ip-prefix-nginx.txt", true);
        BufferedReader br = new BufferedReader(new FileReader("test/test-nginx.txt"));
        String l;
        int n = 0;
        while ((l = br.readLine()) != null) {
            String[] c = l.split("\t", -1);
            assertEquals(tr.selectValue(c[0]), Integer.parseInt(c[1]), "Mismatch in line #" + n);
            n++;
        }
        System.out.println(tr.size());
    }
}
