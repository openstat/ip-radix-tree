This project provides a fast, robust and memory-savvy implementation
of IPv4 radix tree (AKA Patricia trie) in Java.

The API roughly follows Java Collections API and can be easily
demonstrated in a following snippet:

```java
IPv4RadixTree tr=new IPv4RadixTree();
        tr.put(0x0a000000,0xffffff00);
        tr.put(0x0a000000,0xff000000);
        tr.put("10.0.3.0/24");
        boolean v1=tr.containsKey(0x0a202020); // => true, as 10.32.32.32 belongs to 10.0.0.0/8
        boolean v2=tr.containsKey(0x0a000020); // => true, as 10.0.0.32 belongs to 10.0.0.0/24
        boolean v3=tr.containsIp("10.0.3.5"); // => true, as 10.0.3.5 belongs to 10.0.3.0/24
```

## Memory consumption ##

Memory consumption for this implementation is tuned to be the lowest
possible. No Java internal objects or object references are used, as
they consume too much memory per node (on 64-bit JVM, it's something
like 32 bytes per object).

We use pre-allocated simple Java int[] arrays, one for left branch
pointers, one for right branch pointers and one for values. It takes
exactly 2 * 4 byte + 1 bit = 8 bytes + 1 bit per node.

Number of nodes in a tree greatly depends on tree topology: a rough
estimate for generic IPv4 routing trees is something like from 3 up to
8 times the number of IP prefixes.
