This project provides a fast, robust and memory-savvy implementation
of IPv4 radix tree (AKA Patricia trie) in Java.

The API roughly follows Java Collections API and can be easily
demonstrated in a following snippet:

```java
IPv4RadixIntTree tr = new IPv4RadixIntTree();
tr.put(0x0a000000, 0xffffff00, 42);
tr.put(0x0a000000, 0xff000000, 69);
tr.put("10.0.3.0/24", 123);
int v1 = tr.selectValue(0x0a202020); // => 69, as 10.32.32.32 belongs to 10.0.0.0/8
int v2 = tr.selectValue(0x0a000020); // => 42, as 10.0.0.32 belongs to 10.0.0.0/24
int v3 = tr.selectValue("10.0.3.5"); // => 123, as 10.0.3.5 belongs to 10.0.3.0/24
```

## Memory consumption ##

Memory consumption for this implementation is tuned to be the lowest
possible. No Java internal objects or object references are used, as
they consume too much memory per node (on 64-bit JVM, it's something
like 32 bytes per object).

We use pre-allocated simple Java int[] arrays, one for left branch
pointers, one for right branch pointers and one for values. It takes
exactly 3 * 4 = 12 bytes per node.

Number of nodes in a tree greatly depends on tree topology: a rough
estimate for generic IPv4 routing trees is something like from 3 up to
8 times the number of IP prefixes.

For example, a test case included with this distribution has 392415 IP
prefixes and it generates up to 946225 nodes in memory, thus consuming
about 10.8 megabytes of heap.
