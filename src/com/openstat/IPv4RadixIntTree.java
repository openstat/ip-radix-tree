package com.openstat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IPv4RadixIntTree {
    public static final int NO_VALUE = -1;

    private static final long MAX_IPV4_BIT = 0x80000000L;

    public static class Node {
        private Node right, left, parent;
        private int value;
    }

    private Node root;
    private int size;

    public IPv4RadixIntTree() {
        size = 0;
        root = new Node();
        root.value = NO_VALUE;
    }

    public void put(long key, long mask, int value) {
        long bit = MAX_IPV4_BIT;
        Node node = root;
        Node next = root;

        while ((bit & mask) != 0) {
            next = ((key & bit) != 0) ? node.right : node.left;
            if (next == null)
                break;
            bit >>= 1;
            node = next;
        }

        if (next != null) {
//            if (node.value != NO_VALUE) {
//                throw new IllegalArgumentException();
//            }

            node.value = value;
            return;
        }

        while ((bit & mask) != 0) {
            next = new Node();
            next.parent = node;
            next.value = NO_VALUE;

            if ((key & bit) != 0) {
                node.right = next;
            } else {
                node.left = next;
            }

            bit >>= 1;
            node = next;
        }

        node.value = value;
        size++;
    }

    public int selectValue(long key) {
        long bit = MAX_IPV4_BIT;
        int value = NO_VALUE;
        Node node = root;

        while (node != null) {
            if (node.value != NO_VALUE)
                value = node.value;
            node = ((key & bit) != 0) ? node.right : node.left;
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
        IPv4RadixIntTree tr = new IPv4RadixIntTree();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String l;
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
