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

/*
 * This file has been changed and no longer resembles the original file.
 */

package com.openstat;

import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

import static org.testng.Assert.*;

@Test
public class RadixTreeTests {
    @Test
    public void testContainsKey() throws UnknownHostException {
        IPv4RadixTree tr = new IPv4RadixTree( 32 );
        tr.put( 0x0a000000, 0xffffff00 );
        tr.put( 0x0a000000, 0xff000000 );

        assertTrue( tr.containsKey( 0x0a202020 ) );
        assertTrue( tr.containsKey( 0x0a000020 ) );
        assertFalse( tr.containsKey( 0x0b010203 ) );
    }

    @Test
    public void testContainsIp() throws IOException {
        IPv4RadixTree tr = IPv4RadixTree.loadFromLocalFile( "src/test/resources/ip-prefix-base.txt" );

        BufferedReader br = new BufferedReader( new FileReader( "src/test/resources/test-1.txt" ) );

        String l;
        int n = 0;
        while( (l = br.readLine()) != null ) {
            String[] c = l.split( "\t", -1 );
            assertEquals( tr.containsIp( c[0] ), Boolean.parseBoolean( c[1] ), "Mismatch in line #" + n );
            n++;
        }
        System.out.println( tr.size() );
    }
}
