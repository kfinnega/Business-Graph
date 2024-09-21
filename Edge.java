import java.io.Serializable;

public class Edge implements Serializable {
    public int src;
    public int dst;
    public double weight;

    public Edge(int src, int dst, double weight) {
        this.src = src;
        this.dst = dst;
        this.weight = weight;
    }
}
