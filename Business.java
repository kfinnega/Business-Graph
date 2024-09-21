import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Business implements Serializable{
    String business_id;
    String name;
    String city;
    float latitude;
    float longitude;
    String categories;
    String mostSim1;
    String mostSim2;

    public ArrayList<Business> closestNeighbors;

    double simScore1;
    double simScore2;
    Business closest;
    double closestVal = 10000000;
    Business secondCloset;
    double secondClosetVal = 10000000;
    Business thirdClosest;
    double thirdClosestVal = 10000000;
    Business forthClosest;
    double forthClosestVal = 10000000;


    public ArrayList<String> terms;
    HashMap<String, Double> tfidfValues;

    public String getBusiness_id(){ return this.business_id; }

    public String getName(){
        return this.name;
    }

    public void saveToFile(String directory) throws IOException {
        String fileName = directory + "/" + business_id + ".bin";
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(this);

            oos.close();
            fos.close();
        }
    }

    public static Business loadFromFile(String fileName) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(fileName);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Business) ois.readObject();
        }
    }

    @Override
    public String toString() {
        return "Business {" + " name='" + name + " }";
    }
}
