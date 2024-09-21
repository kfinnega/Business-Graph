import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class ExtensibleHashTable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1195464413171125304L;
    public Kmedoids kmedoids;
    public int numOfBiz = 0;
    String bucketDir;
    int size = 2;
    int[] pBuckets; // bucket pointers
    int numOfBuckets = 1;

    public ExtensibleHashTable(String bucketDir) throws IOException, ClassNotFoundException {
        this.bucketDir = bucketDir;
        pBuckets = new int[2];
        pBuckets[0] = 0;
        pBuckets[1] = 0;
        saveBucket(new Bucket(), 0);
    }

    public void add(String key, String value) throws IOException, ClassNotFoundException {
        int hash = key.hashCode();
        int pIndex = hash & (size - 1);
        int bIndex = pBuckets[pIndex];
        Bucket bucket = loadBucket(bIndex);
        if (bucket.isFull()) {
            resize(bIndex);
        }
        bucket.add(hash, value);
        saveBucket(bucket, bIndex);
        numOfBiz++;

    }

    public String get(String e) throws IOException, ClassNotFoundException {
        int hash = e.hashCode();
        int pIndex = hash & (size - 1);
        int bIndex = pBuckets[pIndex];
        Bucket bucket = loadBucket(bIndex);
        return bucket.find(hash);
    }

    private void resize(int lbIndex) throws IOException, ClassNotFoundException {

        ArrayList<Integer> pointers = getPointers(lbIndex);
        if (pointers.size() == 1) {
            int oldsize = size;
            size <<= 1;
            int[] newPBuckets = new int[size];

            int[] pointersToSplit = new int[2];
            for (int i = 0; i < size; i++) {
                if ((i & (oldsize - 1)) == lbIndex) {
                    pointersToSplit[i / oldsize] = i & (oldsize - 1);
                }

                newPBuckets[i] = pBuckets[i & (oldsize - 1)];
            }
            pBuckets = newPBuckets;
            splitBucket(pointersToSplit[0], pointersToSplit[1]);
        } else {
            splitBucket(pointers.get(0), pointers.get(1));
        }
    }

    private void splitBucket(int p1, int p2) throws ClassNotFoundException, IOException {
        if (p1 == p2 || pBuckets[p1] != pBuckets[p2]) {
            return;
        }
        int bIndex = pBuckets[p1];
        Bucket oldBucket = loadBucket(bIndex);
        Bucket newBucket = new Bucket();

        for (Node node : oldBucket.getNodeArr()) {
            if (node == null) {
                break;
            }
            if ((node.key & (size - 1)) == p2) {
                oldBucket.remove(node.key);
                newBucket.add(node.key, node.value);
            }
        }

        pBuckets[p2] = numOfBuckets++;
        saveBucket(oldBucket, bIndex);
        saveBucket(newBucket, pBuckets[p2]);
    }

    private ArrayList<Integer> getPointers(int bIndex) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < pBuckets.length; i++) {
            if (pBuckets[i] == bIndex) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public String getFilePath(int id) {
        return bucketDir + "/bucket-" + id;
    }

    private Bucket loadBucket(int bIndex) throws IOException, ClassNotFoundException {
        try (FileInputStream file = new FileInputStream(getFilePath(bIndex));
             ObjectInputStream in = new ObjectInputStream(file)) {
            return (Bucket) in.readObject();
        }
    }

    private void saveBucket(Bucket bucket, int bIndex) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(getFilePath(bIndex));
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(bucket);
        }
    }

    public void printAll() throws IOException, ClassNotFoundException {
        HashSet<Integer> visitedBuckets = new HashSet<>();

        for (int i = 0; i < size; i++) {
            int bIndex = pBuckets[i];
            if (!visitedBuckets.contains(bIndex)) {
                visitedBuckets.add(bIndex);

                FileInputStream file = new FileInputStream(getFilePath(bIndex));
                ObjectInputStream in = new ObjectInputStream(file);
                Bucket bucket = (Bucket) in.readObject();
                in.close();
                file.close();

                Node[] arr = bucket.getNodeArr();
                for (int j = 0; j < bucket.size; j++) {
                    System.out.println("Key: " + arr[j].key + ", Value: " + arr[j].value);
                }
            }
        }
    }

    public void getBiztfidfs() throws IOException, ClassNotFoundException {
        HashSet<Integer> visitedBuckets = new HashSet<>();

        for (int i = 0; i < size; i++) {
            int bIndex = pBuckets[i];
            if (!visitedBuckets.contains(bIndex)) {
                visitedBuckets.add(bIndex);

                FileInputStream file = new FileInputStream(getFilePath(bIndex));
                ObjectInputStream in = new ObjectInputStream(file);
                Bucket bucket = (Bucket) in.readObject();
                in.close();
                file.close();

                Node[] arr = bucket.getNodeArr();
                for (int j = 0; j < bucket.size; j++) {
                    String filePath = arr[j].value;
                    FileInputStream bizfile = new FileInputStream(filePath);
                    ObjectInputStream bizin = new ObjectInputStream(bizfile);
                    Business business = (Business) bizin.readObject();
                }
            }
        }

    }

    public void saveToFile() throws IOException {
        FileOutputStream fOut = new FileOutputStream("data/ht.bin");
        ObjectOutputStream out = new ObjectOutputStream(fOut);
        out.writeObject(this);
        out.close();
        fOut.close();
    }

    public static ExtensibleHashTable loadFromFile() throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream("data/ht.bin");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (ExtensibleHashTable) ois.readObject();
        }
    }


    public String getRandomValue() throws IOException, ClassNotFoundException {
        Random random = new Random();
        int randomBucketIndex = -1;
        Bucket randomBucket = null;

        // Select a non-empty random bucket
        while (randomBucket == null || randomBucket.size == 0) {
            randomBucketIndex = random.nextInt(numOfBuckets);
            randomBucket = loadBucket(randomBucketIndex);
        }

        // Select a random node within the bucket
        int randomNodeIndex = random.nextInt(randomBucket.size);
        Node randomNode = randomBucket.getNodeArr()[randomNodeIndex];

        // Return the value associated with the randomly selected node
        return randomNode.value;
    }

    public void createClusters(Business[] medoids) throws IOException, ClassNotFoundException {
        CosSimCalc cosSim = new CosSimCalc();
        Business business;
        String path = "data/clusters/";
        HashSet<Integer> visitedBuckets = new HashSet<>();
        Business medoid;
        Business comparee;
        int bestFitMedoid = 0;
        double bestFitvalue = 0.0;
        double cosSimVal = 0.0;

        for (int i = 0; i < size; i++) {
            int bIndex = pBuckets[i];
            if (!visitedBuckets.contains(bIndex)) {
                visitedBuckets.add(bIndex);

                FileInputStream file = new FileInputStream(getFilePath(bIndex));
                ObjectInputStream in = new ObjectInputStream(file);
                Bucket bucket = (Bucket) in.readObject();
                in.close();
                file.close();

                Node[] arr = bucket.getNodeArr();
                for (int j = 0; j < bucket.size; j++) {
                    comparee = Business.loadFromFile("data/restaurants/"+ arr[j].value);
                    bestFitvalue = 0.0;
                    bestFitMedoid = 0;

                    for (int k = 0; k < medoids.length; k++) {
                        medoid = medoids[k];
                        cosSimVal = cosSim.compareBusiness(medoid, comparee);

                        if (cosSimVal >= bestFitvalue) {
                            bestFitvalue = cosSimVal;
                            bestFitMedoid = k + 1;
                        }
                    }
                    String clusterFile = path + "cluster" + bestFitMedoid + ".txt";

                    try {
                        FileWriter fw = new FileWriter(clusterFile, true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter pw = new PrintWriter(bw);

                        business = Business.loadFromFile(arr[j].value);
                        pw.println(business.name);

                        pw.close();
                        bw.close();
                        fw.close();

                    } catch (IOException io) {
                        System.out.println("Can't find cluster file");
                    }
                }
            }
        }
    }
}