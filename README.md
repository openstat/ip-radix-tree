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

```java
IPv6RadixIntTree tr = new IPv6RadixIntTree();
tr.put("FF05::B3/24",43);
tr.put("FF02::B3", "FFFF::", 42);
int v1 = tr.selectValue("FF05::"); 
int v2 = tr.selectValue("FF02::B2");
```
## Reference ##

https://github.com/openstat/ip-radix-tree
