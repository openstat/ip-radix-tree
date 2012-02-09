package com.openstat;

public class IPv4RadixIntTree {
    public static final int NO_VALUE = -1;

    public static class Node {
        Node right, left, parent;
        int value;
    }

    private Node root;
    private int size;

    public IPv4RadixIntTree() {
        size = 0;
        root = new Node();
        root.value = NO_VALUE;
    }

    public void put(long key, long mask, int value) {
        long bit = 0x80000000L;
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
        long bit = 0x80000000L;
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

    public int size() { return size; }
}
