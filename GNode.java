import java.io.Serializable;

public class GNode implements Serializable {
    public Business business;
    public int num;

    public GNode(Business business) {
        this.business = business;
    }



}
