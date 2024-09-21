import java.io.Serializable;

public class Bucket implements Serializable {
    public Node[] nodeArr;
    public int size;

    public Bucket() {
        nodeArr = new Node[32];
        size = 0;
    }

    public Node[] getNodeArr() {
        return nodeArr;
    }

    public void add(int key, String value) {
        if (isFull()) {
            return;
        }
        nodeArr[size++] = new Node(key, value);
    }

    public void remove(int key) {
        for (int i = 0; i < size; i++) {
            if (nodeArr[i].key == key) {
                nodeArr[i] = nodeArr[size - 1];
                nodeArr[--size] = null;
            }
        }
    }

    public String find(int key) {
        for (int i = 0; i < size; i++) {
            if (nodeArr[i].key == key) {
                return nodeArr[i].value;
            }
        }
        return null;
    }

    public boolean isFull() {
        return size >= nodeArr.length;
    }
}
