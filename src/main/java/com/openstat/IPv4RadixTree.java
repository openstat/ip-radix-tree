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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * A minimalistic, memory size-savvy and fairly fast radix tree (AKA Patricia trie) implementation that uses IPv4
 * addresses with netmasks as keys without values.
 * <p>
 * This tree is generally uses in read-only manner: there are no key removal operation and the whole thing works best in
 * pre-allocated fashion.
 */
public class IPv4RadixTree {

    private static final int NULL_PTR = -1;
    private static final int ROOT_PTR = 0;
    private static final int BIT_PER_VALUE = 32;

    private static final long MAX_IPV4_BIT = 0x80000000L;

    private int[] rights;
    private int[] lefts;
    private int[] values;

    private int allocatedSize;
    private int size;

    /**
     * Initializes IPv4 radix tree with default capacity of 1024 nodes. It should be sufficient for small databases.
     */
    public IPv4RadixTree() {
        this( 1024 );
    }

    /**
     * Initializes IPv4 radix tree with a given capacity.
     *
     * @param allocatedSize initial capacity to allocate. Must be dividable by 32.
     */
    public IPv4RadixTree( int allocatedSize ) {
        if( allocatedSize <= 0 || allocatedSize % BIT_PER_VALUE != 0 )
            throw new RuntimeException( "allocatedSize must be larger than 0 and dividable by " + BIT_PER_VALUE + "!" );

        this.allocatedSize = allocatedSize;

        rights = new int[this.allocatedSize];
        lefts = new int[this.allocatedSize];
        values = new int[this.allocatedSize / BIT_PER_VALUE];

        size = 1;
        lefts[0] = NULL_PTR;
        rights[0] = NULL_PTR;
    }


    /**
     * Puts a key with network mask into the tree.
     *
     * @param key  IPv4 network prefix
     * @param mask IPv4 netmask in networked byte order format (for example, 0xffffff00L = 4294967040L corresponds to
     *             255.255.255.0 AKA /24 network bitmask)
     */
    public void put( long key, long mask ) {
        long bit = MAX_IPV4_BIT;
        int node = ROOT_PTR;
        int next = ROOT_PTR;

        while( (bit & mask) != 0 ) {
            next = ((key & bit) != 0) ? rights[node] : lefts[node];
            if( next == NULL_PTR )
                break;
            bit >>= 1;
            node = next;
        }

        if( next != NULL_PTR ) {
            setValue( node );
            return;
        }

        while( (bit & mask) != 0 ) {
            if( size == allocatedSize )
                expandAllocatedSize();

            next = size;
            unsetValue( next );
            rights[next] = NULL_PTR;
            lefts[next] = NULL_PTR;

            if( (key & bit) != 0 ) {
                rights[node] = next;
            } else {
                lefts[node] = next;
            }

            bit >>= 1;
            node = next;
            size++;
        }

        setValue( node );
    }

    private boolean getValue( int index ) {
        int arrIndex = index / BIT_PER_VALUE;
        int bitIndex = index % BIT_PER_VALUE;
        return ((values[arrIndex] >> bitIndex) & 1) == 1;
    }

    private void setValue( int index ) {
        int arrIndex = index / BIT_PER_VALUE;
        int bitIndex = index % BIT_PER_VALUE;
        values[arrIndex] |= 1 << bitIndex;
    }

    private void unsetValue( int index ) {
        int arrIndex = index / BIT_PER_VALUE;
        int bitIndex = index % BIT_PER_VALUE;
        values[arrIndex] &= ~(1 << bitIndex);
    }

    private void expandAllocatedSize() {
        int oldSize = allocatedSize;
        allocatedSize = allocatedSize * 2;

        int[] newLefts = new int[allocatedSize];
        System.arraycopy( lefts, 0, newLefts, 0, oldSize );
        lefts = newLefts;

        int[] newRights = new int[allocatedSize];
        System.arraycopy( rights, 0, newRights, 0, oldSize );
        rights = newRights;

        int[] newValues = new int[allocatedSize / BIT_PER_VALUE];
        System.arraycopy( values, 0, newValues, 0, oldSize / BIT_PER_VALUE );
        values = newValues;
    }

    /**
     * Checks if a given IPv4 address is covered by the tree. Traversing tree and choosing most specific value available
     * for a given address.
     *
     * @param key IPv4 address to look up
     * @return true if IPv4 address is covered by the tree
     */
    public boolean containsKey( long key ) {
        long bit = MAX_IPV4_BIT;
        boolean value = false;
        int node = ROOT_PTR;

        while( node != NULL_PTR ) {
            if( getValue( node ) )
                value = true;
            node = ((key & bit) != 0) ? rights[node] : lefts[node];
            bit >>= 1;
        }

        return value;
    }

    /**
     * Puts an IPv4 network into the tree, using a string representation of IPv4 prefix.
     *
     * @param ipNet IPv4 network as a string in form of "a.b.c.d/e", where a, b, c, d
     *              are IPv4 octets (in decimal) and "e" is a netmask in CIDR notation
     * @throws UnknownHostException
     */
    public void put( String ipNet ) throws UnknownHostException {
        int pos = ipNet.indexOf( '/' );
        String ipStr = ipNet.substring( 0, pos );
        long ip = inet_aton( ipStr );

        String netmaskStr = ipNet.substring( pos + 1 );
        int cidr = Integer.parseInt( netmaskStr );
        long netmask = ((1L << (32 - cidr)) - 1L) ^ 0xffffffffL;

        put( ip, netmask );
    }

    /**
     * Checks if a given IPv4 address is covered by the tree. Traversing tree and choosing most specific value available
     * for a given address.
     *
     * @param ipAddress IPv4 address to look up, in string form (i.e. "a.b.c.d")
     * @return true if IPv4 address is covered by the tree
     * @throws UnknownHostException
     */
    public boolean containsIp( String ipAddress ) throws UnknownHostException {
        return containsKey( inet_aton( ipAddress ) );
    }

    /**
     * Helper function that reads IPv4 radix tree from a local file.
     *
     * @param filename name of a local file to read
     * @return a fully constructed IPv4 radix tree from that file
     * @throws IOException
     */
    public static IPv4RadixTree loadFromLocalFile( String filename ) throws IOException {
        int countLinesInLocalFile = countLinesInLocalFile( filename );
        IPv4RadixTree tr = new IPv4RadixTree( countLinesInLocalFile + BIT_PER_VALUE - (countLinesInLocalFile % BIT_PER_VALUE) );
        BufferedReader br = new BufferedReader( new FileReader( filename ) );
        String l;

        while( (l = br.readLine()) != null )
            tr.put( l );

        return tr;
    }

    private static long inet_aton( String ipStr ) throws UnknownHostException {
        ByteBuffer bb = ByteBuffer.allocate( 8 );
        bb.putInt( 0 );
        bb.put( InetAddress.getByName( ipStr ).getAddress() );
        bb.rewind();
        return bb.getLong();
    }

    private static int countLinesInLocalFile( String filename ) throws IOException {
        BufferedReader br = new BufferedReader( new FileReader( filename ) );
        int n = 0;
        while( br.readLine() != null )
            n++;
        return n;
    }

    /**
     * Returns a size of tree in number of nodes (not number of prefixes stored).
     *
     * @return a number of nodes in current tree
     */
    public int size() {
        return size;
    }
}