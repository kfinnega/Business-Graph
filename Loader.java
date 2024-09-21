import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Loader {

    HashMap<String, Business> businessMap;
    FT docs;
    ExtensibleHashTable ht;

    public Loader() {
        this.businessMap = new HashMap<>();
        this.docs = new FT();
        try {
            this.ht = new ExtensibleHashTable("data/buckets");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.out.println("ExtensibleHashTable Class Not Found");
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        float start = System.nanoTime();
        float tstart = System.nanoTime();

        FT docs = new FT();
        HashMap<String, Business> businessMap = new HashMap<>();
        createDirectories();

        int count = 0;
        Gson gson = new GsonBuilder().setLenient().create();
        BufferedReader reader = null;

        try {
            ExtensibleHashTable ht = new ExtensibleHashTable("data/buckets");
            reader = new BufferedReader(new FileReader("data/yelp_academic_dataset_business.json"));

            String currentLine;
            Business business;
            Business comparee;

            while (count < 10000) {
                currentLine = reader.readLine();
                business = gson.fromJson(currentLine, Business.class);
                String categories = business.categories;
                if (businessMap.containsKey(business.name)) continue;

                if (categories != null && categories.contains("Restaurants")){
                    count++;
                    businessMap.put(business.name, business);
                    business.terms = new ArrayList<>();
                    String[] splitter = business.categories.split(", ");
                    for (String category : splitter) {
                        business.terms.add(category);
                        docs.add(category);
                    }
                }
            }

            float end = System.nanoTime();
            System.out.println("Business Map Created: Size { " + businessMap.size() + " } || Created FT And Lists Created TF-IDF { " + ((end-start)/1000000000) + " seconds }");
            start = System.nanoTime();

            TfidfCalc tfidfCalc = new TfidfCalc();
            HaversineCalc hcalc = new HaversineCalc();

            for(Map.Entry<String,Business> entry : businessMap.entrySet()) {
                business = entry.getValue();
                business.closestNeighbors = new ArrayList<>();
                business.tfidfValues = new HashMap<>();
                for(String term : business.terms) {
                    double tfidf = tfidfCalc.tfIdf(business.terms, docs, term);
                    business.tfidfValues.put(term, tfidf);
                }
                for(Map.Entry<String,Business> comp : businessMap.entrySet()) {
                    comparee = comp.getValue();

                    if (!comparee.name.equals(business.name)) {
                        double distance = hcalc.distance(business,comparee);
                        if (distance < business.closestVal) {
                            business.forthClosest = business.thirdClosest;
                            business.forthClosestVal = business.thirdClosestVal;
                            business.thirdClosest = business.secondCloset;
                            business.thirdClosestVal = business.secondClosetVal;
                            business.secondCloset = business.closest;
                            business.secondClosetVal = business.closestVal;
                            business.closest = comparee;
                            business.closestVal = distance;
                        } else if (distance < business.secondClosetVal) {
                            business.forthClosest = business.thirdClosest;
                            business.forthClosestVal = business.thirdClosestVal;
                            business.thirdClosest = business.secondCloset;
                            business.thirdClosestVal = business.secondClosetVal;
                            business.secondCloset = comparee;
                            business.secondClosetVal = distance;
                        } else if (distance < business.thirdClosestVal) {
                            business.forthClosest = business.thirdClosest;
                            business.forthClosestVal = business.thirdClosestVal;
                            business.thirdClosest = comparee;
                            business.thirdClosestVal = distance;
                        } else if (distance < business.forthClosestVal) {
                            business.forthClosest = comparee;
                            business.forthClosestVal = distance;
                        }
                    }
                }
                business.closestNeighbors = new ArrayList<>();
                business.closestNeighbors.add(business.closest);
                business.closestNeighbors.add(business.secondCloset);
                business.closestNeighbors.add(business.thirdClosest);
                business.closestNeighbors.add(business.forthClosest);
            }

            end = System.nanoTime();
            System.out.println("Four Closest Business Added || TF-IDF Values Map Added { " + ((end-start)/1000000000) + " seconds }");
            start = System.nanoTime();

            CosSimCalc cosSimCalc = new CosSimCalc();

            for (Map.Entry<String,Business> entry : businessMap.entrySet()) {
                business = entry.getValue();
                for (Map.Entry<String,Business> comp : businessMap.entrySet()) {
                    comparee = comp.getValue();
                    double cosim = cosSimCalc.compareBusiness(business,comparee);
                    if (cosim > business.simScore1) {
                        business.simScore1 = cosim;
                        business.mostSim1 = comparee.name;
                    } else if (cosim > business.simScore2) {
                        business.simScore2 = cosim;
                        business.mostSim2 = comparee.name;
                    }
                }
                String fileName = business.business_id + ".bin";
                ht.add(business.name, fileName);
                business.saveToFile("data/restaurants");
            }

            end = System.nanoTime();
            System.out.println("Two Most Similar Added || Businesses Added to HT { " + ((end-start)/1000000000) + " seconds }");
            start = System.nanoTime();

            ArrayList<ArrayList<String>> clusters = new ArrayList<>();
            ArrayList<Business> medoids = new ArrayList<>();

            medoids.add(Business.loadFromFile("data/restaurants/G5kWtADcIMy5CgrjUerfnA.bin"));
            medoids.add(Business.loadFromFile("data/restaurants/KdQmzwomvjLiMzD4UnLzBA.bin"));
            medoids.add(Business.loadFromFile("data/restaurants/fs6f7Esb6InIC2QhwWi_hQ.bin"));
            medoids.add(Business.loadFromFile("data/restaurants/4BKKBx50oZaEczazjZ1ZEQ.bin"));
            medoids.add(Business.loadFromFile("data/restaurants/DJ1ga9ulOtxtrx1QRJ92SA.bin"));
            medoids.add(Business.loadFromFile("data/restaurants/NjD-V9pdH7_V8Y8PGl-csw.bin"));

            ArrayList<String> cluster1 = new ArrayList<>();
            ArrayList<String> cluster2 = new ArrayList<>();
            ArrayList<String> cluster3 = new ArrayList<>();
            ArrayList<String> cluster4 = new ArrayList<>();
            ArrayList<String> cluster5 = new ArrayList<>();
            ArrayList<String> cluster6 = new ArrayList<>();

            for (Map.Entry<String, Business> entry : businessMap.entrySet()){
                int bestIndex = -1;
                double bestMedoidScore = -1;
                business = entry.getValue();

                if (medoids.contains(business)) continue;
                int scoreCalcCount = 0;
                for (int i = 0; i < medoids.size(); i++) {
                    double score = cosSimCalc.compareBusiness(business, medoids.get(i));
                    scoreCalcCount++;
                    if (score >= bestMedoidScore) {

                        bestMedoidScore = score;
                        bestIndex = i;
                    }
                }

                if (bestIndex == 0) {
                    cluster1.add(business.name);
                } else if (bestIndex == 1) {
                    cluster2.add(business.name);
                } else if (bestIndex == 2) {
                    cluster3.add(business.name);
                } else if (bestIndex == 3) {
                    cluster4.add(business.name);
                } else if (bestIndex == 4) {
                    cluster5.add(business.name);
                } else if (bestIndex == 5) {
                    cluster6.add(business.name);
                }
            }

            clusters.add(cluster1);
            clusters.add(cluster2);
            clusters.add(cluster3);
            clusters.add(cluster4);
            clusters.add(cluster5);
            clusters.add(cluster6);

            createClusterFiles();
            fillClusters(clusters);
            ht.saveToFile();

            end = System.nanoTime();
            System.out.println("Clusters Made || HT Made { " + ((end-start)/1000000000) + " seconds }");
            start = System.nanoTime();

            BusinessGraph graph = new BusinessGraph(); // Create graph

            // Add businesses as nodes to the graph
            for (Map.Entry<String,Business> entry : businessMap.entrySet()) {
                business = entry.getValue();
                graph.addNode(new GNode(business));
            }

            System.out.println("Nodes Created and Added\nGraph Size: { " + graph.added.size() + " }");

            // Add four closest as edges for each business
            CosSimCalc csc = new CosSimCalc();
            double score = 0.0;
            for (Map.Entry<String,Business> entry : businessMap.entrySet()) {
                business = entry.getValue();
                GNode src = graph.getNode(business.name);
                GNode dst;
                ArrayList<Business> neighbors = business.closestNeighbors;
                for(Business neighbor : neighbors) {
                    dst = graph.getNode(neighbor.name);
                    score = csc.compareBusiness(business, neighbor);
                    graph.addEdge(src.num, dst.num, score);
                }
                count++;
            }

            end = System.nanoTime();
            System.out.println("Graph Created { " + ((end-start)/1000000000) + " seconds }");
            start = System.nanoTime();

            List<List<String>> disjointSets = graph.getDisjointSets();
            serializeDisjointSetsToFile(disjointSets, "data/disjointed_sets.bin");
            serializeBusinessGraph(graph, "data/graph.bin");

            end = System.nanoTime();
            System.out.println("Disjointed Sets found { " + ((end-start)/1000000000) + " seconds }" );

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        float tend = System.nanoTime();
        System.out.println("SUCCESS\nTOTAL TIME: {" + ((tend - tstart) /1000000000) + "} SECONDS");

    }

    public static void serializeDisjointSetsToFile(List<List<String>> disjointSets, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(disjointSets);

            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void serializeBusinessGraph(BusinessGraph graph, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(graph);

            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int avgDisJointSetSize(List<List<String>> djs) {
        int size = 0;
        for(List<String> set : djs) {
            size += set.size();
        }
        return size / djs.size();
    }

    public static void createDirectories() {
        String[] dirNames = {"buckets", "restaurants", "clusters"};

        for (String dirName : dirNames) {
            createDirectory(dirName);
        }
    }

    private static void createDirectory(String dirName) {
        Path dirPath = Paths.get("data/" + dirName);

        if (!Files.exists(dirPath)) {
            try {
                Files.createDirectory(dirPath);
            } catch (IOException e) {
                System.err.println("Failed to create directory '" + dirName + "': " + e.getMessage());
            }
        }
    }

    public static void deleteDirectories() {
        String[] dirNames = {"buckets", "restaurants", "clusters"};
        for (String dirName : dirNames) {
            deleteDirectory(dirName);
        }
        deleteDirectory("ht.bin");
       // deleteDirectory("disjointed_sets.bin");
    }

    private static void deleteDirectory(String dirName) {
        Path dirPath = Paths.get("data/" + dirName);

        if (Files.exists(dirPath)) {
            try {
                Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                System.err.println("Failed to delete directory '" + dirName + "': " + e.getMessage());
            }
        }
    }

    public static void createClusterFiles() {
        for (int i = 1; i < 6; i++) {
            Path filePath = Paths.get("data/clusters/cluster" + (i) + ".txt");
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void fillClusters(ArrayList<ArrayList<String>> clusters) {
        int i = 1;
        try {
            for (ArrayList<String> cluster : clusters) {
                PrintWriter pw = new PrintWriter(new FileOutputStream("data/clusters/cluster" + i + ".txt"));
                for(String name : cluster) {
                    pw.println(name);
                }
                pw.close();
                i++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int[] getClusterSizes(ArrayList<ArrayList<String>> clusters) {
        int[] retVal = new int[clusters.size()];
        int i = 0;
        for (ArrayList<String> cluster : clusters) {
            retVal[i] = cluster.size();
            i++;
        }
        return retVal;
    }

}