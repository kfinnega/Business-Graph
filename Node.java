import java.io.Serializable;

public class Node implements Serializable {
    public int key;
    public String value;

    Node(int k, String v) {
        key = k;
        value = v;
    }
}
