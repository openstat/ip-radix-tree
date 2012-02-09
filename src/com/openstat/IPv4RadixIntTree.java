package com.openstat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPv4RadixIntTree {
    public static final int NO_VALUE = -1;
    private static final int NULL_PTR = -1;
    private static final int ROOT_PTR = 0;

    private static final long MAX_IPV4_BIT = 0x80000000L;

    private int[] rights;
    private int[] lefts;
    private int[] values;

    private int allocatedSize;
    private int size;

    public IPv4RadixIntTree(int allocatedSize) {
        this.allocatedSize = allocatedSize;

        rights = new int[this.allocatedSize];
        lefts = new int[this.allocatedSize];
        values = new int[this.allocatedSize];

        size = 1;
        lefts[0] = NULL_PTR;
        rights[0] = NULL_PTR;
        values[0] = NO_VALUE;
    }

    public void put(long key, long mask, int value) {
        long bit = MAX_IPV4_BIT;
        int node = ROOT_PTR;
        int next = ROOT_PTR;

        while ((bit & mask) != 0) {
            next = ((key & bit) != 0) ? rights[node] : lefts[node];
            if (next == NULL_PTR)
                break;
            bit >>= 1;
            node = next;
        }

        if (next != NULL_PTR) {
//            if (node.value != NO_VALUE) {
//                throw new IllegalArgumentException();
//            }

            values[node] = value;
            return;
        }

        while ((bit & mask) != 0) {
            if (size == allocatedSize)
                expandAllocatedSize();

            next = size;
            values[next] = NO_VALUE;
            rights[next] = NULL_PTR;
            lefts[next] = NULL_PTR;

            if ((key & bit) != 0) {
                rights[node] = next;
            } else {
                lefts[node] = next;
            }

            bit >>= 1;
            node = next;
            size++;
        }

        values[node] = value;
    }

    private void expandAllocatedSize() {
        int oldSize = allocatedSize;
        allocatedSize = allocatedSize * 2;

        int[] newLefts = new int[allocatedSize];
        System.arraycopy(lefts, 0, newLefts, 0, oldSize);
        lefts = newLefts;

        int[] newRights = new int[allocatedSize];
        System.arraycopy(rights, 0, newRights, 0, oldSize);
        rights = newRights;

        int[] newValues = new int[allocatedSize];
        System.arraycopy(values, 0, newValues, 0, oldSize);
        values = newValues;
    }

    public int selectValue(long key) {
        long bit = MAX_IPV4_BIT;
        int value = NO_VALUE;
        int node = ROOT_PTR;

        while (node != NULL_PTR) {
            if (values[node] != NO_VALUE)
                value = values[node];
            node = ((key & bit) != 0) ? rights[node] : lefts[node];
            bit >>= 1;
        }

        return value;
    }

    public void put(String ipNet, int value) throws UnknownHostException {
        int pos = ipNet.indexOf('/');
        String ipStr = ipNet.substring(0, pos);
        long ip = inet_aton(ipStr);

        String netmaskStr = ipNet.substring(pos + 1);
        int cidr = Integer.parseInt(netmaskStr);
        long netmask =  ((1L << (32 - cidr)) - 1L) ^ 0xffffffffL;

        put(ip, netmask, value);
    }

    public int selectValue(String ipStr) throws UnknownHostException {
        return selectValue(inet_aton(ipStr));
    }

    public static IPv4RadixIntTree loadFromLocalFile(String filename) throws IOException {
        BufferedReader br;
        String l;

        br = new BufferedReader(new FileReader(filename));
        int n = 0;
        while ((l = br.readLine()) != null) {
            n++;
        }

        IPv4RadixIntTree tr = new IPv4RadixIntTree(n);
        br = new BufferedReader(new FileReader(filename));
        while ((l = br.readLine()) != null) {
            String[] c = l.split("\t", -1);
            tr.put(c[0], Integer.parseInt(c[1]));
        }

        return tr;
    }

    private static long inet_aton(String ipStr) throws UnknownHostException {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(0);
        bb.put(InetAddress.getByName(ipStr).getAddress());
        bb.rewind();
        return bb.getLong();
    }

    public int size() { return size; }
}
