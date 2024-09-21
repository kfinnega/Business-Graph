import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Kmedoids {
    private final ArrayList<Business> data;
    private final int k;
    private final int maxIterations;
    private final CosSimCalc cosSimCalc;

    public Kmedoids(ArrayList<Business> data, int k, int maxIterations) {
        this.data = data;
        this.k = k;
        this.maxIterations = maxIterations;
        this.cosSimCalc = new CosSimCalc();
    }

    public ArrayList<Business> cluster() {
        ArrayList<Business> medoids = initializeMedoids();
        ArrayList<Business> prevMedoids;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            ArrayList<ArrayList<Business>> clusters = assignToClosestMedoid(medoids);
            prevMedoids = new ArrayList<>(medoids);
            medoids = calculateNewMedoids(clusters);

            if (medoids.equals(prevMedoids)) {
                break;
            }
        }

        return medoids;
    }

    private ArrayList<Business> initializeMedoids() {
        Set<Integer> medoidIndices = new HashSet<>();
        while (medoidIndices.size() < k) {
            medoidIndices.add((int) (Math.random() * data.size()));
        }

        ArrayList<Business> medoids = new ArrayList<>();
        for (Integer index : medoidIndices) {
            medoids.add(data.get(index));
        }
        return medoids;
    }

    private ArrayList<ArrayList<Business>> assignToClosestMedoid(ArrayList<Business> medoids) {
        ArrayList<ArrayList<Business>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<>());
        }

        for (Business business : data) {
            double maxCosSim = Double.NEGATIVE_INFINITY;
            int bestClusterIndex = -1;

            for (int i = 0; i < medoids.size(); i++) {
                double cosSim = cosSimCalc.compareBusiness(business, medoids.get(i));
                if (cosSim > maxCosSim) {
                    maxCosSim = cosSim;
                    bestClusterIndex = i;
                }
            }

            clusters.get(bestClusterIndex).add(business);
        }

        return clusters;
    }

    private ArrayList<Business> calculateNewMedoids(ArrayList<ArrayList<Business>> clusters) {
        ArrayList<Business> newMedoids = new ArrayList<>();

        for (ArrayList<Business> cluster : clusters) {
            if (cluster.isEmpty()) {
                // Assign a random data point as a medoid for the empty cluster
                newMedoids.add(data.get((int) (Math.random() * data.size())));
            } else {
                double minTotalCost = Double.POSITIVE_INFINITY;
                Business bestMedoid = null;

                for (Business candidateMedoid : cluster) {
                    double totalCost = 0;

                    for (Business business : cluster) {
                        double cosSim = cosSimCalc.compareBusiness(candidateMedoid, business);
                        totalCost += 1 - cosSim; // Since Cosine Similarity ranges from -1 to 1, we can use 1 - cosSim as the distance measure
                    }

                    if (totalCost < minTotalCost) {
                        minTotalCost = totalCost;
                        bestMedoid = candidateMedoid;
                    }
                }
                newMedoids.add(bestMedoid);
            }
        }

        return newMedoids;
    }

}