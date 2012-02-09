package com.openstat;

import java.io.IOException;
import java.util.Random;

import org.testng.annotations.Test;

public class BenchmarkTests {
    private static final long SLEEP_INTERVAL = 100;
    public static int M = 10;

    public static void gc() {
        for (int i = 0; i < 3; i++) {
            try {
                System.gc();
                Thread.currentThread().sleep(SLEEP_INTERVAL);
                System.runFinalization();
                Thread.currentThread().sleep(SLEEP_INTERVAL);
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
    }

    @Test public void benchmarkFromFileTest() throws IOException {
        gc();
        long m1 = Runtime.getRuntime().freeMemory();
        IPv4RadixIntTree tr[] = new IPv4RadixIntTree[M];
        for (int i = 0; i < M; i++)
            tr[i] = IPv4RadixIntTree.loadFromLocalFile("test/ip-prefix-base.txt");
        gc();
        long m2 = Runtime.getRuntime().freeMemory();
        System.out.format(
                "%d * %d elements\n%d bytes\n%.2f bytes/element\n",
                M, tr[0].size(),
                m1 - m2,
                (double) (m1 - m2) / (M * tr[0].size())
        );
    }

    int N = 1000000;

    @Test public void benchmarkRandomTest() throws IOException {
        Random rnd = new Random(42);

        gc();
        long m1 = Runtime.getRuntime().freeMemory();

        IPv4RadixIntTree tr = new IPv4RadixIntTree(500000);

        for (int i = 0; i < N; i++) {
            int cidr = rnd.nextInt(33);
            long netmask =  ((1L << (32 - cidr)) - 1L) ^ 0xffffffffL;
            tr.put(rnd.nextLong() & 0xffffffff, netmask, rnd.nextInt());
        }

        gc();
        long m2 = Runtime.getRuntime().freeMemory();

        System.out.format(
                "%d random elements\n%d bytes\n%.2f bytes/element\n",
                tr.size(),
                m1 - m2,
                (double) (m1 - m2) / tr.size()
        );
    }
}
