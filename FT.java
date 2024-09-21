import java.io.*;

public class FT implements java.io.Serializable {
    double totalCount = 0;

    static final class Node {
        String key;
        Node next;
        int count;
        double value;

        Node(String k, int c, Node n) {
            key = k;
            count = c;
            next = n;
        }
    }

    public Node[] table = new Node[8]; // always a power of 2
    int size = 0;

    double getCount(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key))
                return e.count;
        }
        return 0;
    }

    double getValue(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key))
                return e.value;
        }
        return 0;
    }

    int countsSum() {
        int sum = 0;
        for (int i = 0; i < table.length; ++i) {
            for (Node e = table[i]; e != null; e = e.next) {
                sum += e.count;
            }
        }
        return sum;
    }

    void add(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);
        for (Node e = table[i]; e != null; e = e.next) {
            if (key.equals(e.key)) {
                ++e.count;
                ++totalCount;
                return;
            }
        }
        table[i] = new Node(key, 1, table[i]);
        totalCount++;
        ++size;
        if ((float) size / table.length >= 0.75f)
            resize();
    }

    void resize() {
        Node[] oldTable = table;
        int oldCapacity = oldTable.length;
        int newCapacity = oldCapacity << 1;
        Node[] newTable = new Node[newCapacity];
        for (int i = 0; i < oldCapacity; ++i) {
            for (Node e = oldTable[i]; e != null; e = e.next) {
                int h = e.key.hashCode();
                int j = h & (newTable.length - 1);
                newTable[j] = new Node(e.key, e.count, newTable[j]);
            }
        }
        table = newTable;
    }

    void remove(String key) {
        int h = key.hashCode();
        int i = h & (table.length - 1);
        Node e = table[i], p = null;
        while (e != null) {
            if (key.equals(e.key)) {
                if (p == null)
                    table[i] = e.next;
                else
                    p.next = e.next;
                break;
            }
            p = e;
            e = e.next;
        }
    }

    void printAll() {
        for (int i = 0; i < table.length; ++i)
            for (Node e = table[i]; e != null; e = e.next)
                System.out.println(e.key + ": " + e.count + ", Value: " + e.value);
    }

    private void writeObject(ObjectOutputStream s) throws Exception {
        s.defaultWriteObject();
        s.writeInt(size);
        for (int i = 0; i < table.length; ++i) {
            for (Node e = table[i]; e != null; e = e.next) {
                s.writeObject(e.key);
            }
        }
    }
}