import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class Application extends JFrame {

    private JTextField nameTextField;
    private JButton submitButton;
    private JLabel disjointedSetsLabel;
    private JTextArea displayLabel;
    private ExtensibleHashTable ht;
    private List<List<String>> disjointedSets;
    private BusinessGraph graph;
    private int numOfDJS;

    public Application() {

        try {
            ht = ExtensibleHashTable.loadFromFile();
            disjointedSets = deserializeDJS("data/disjointed_sets.bin");
            graph = deserializeGraph("data/graph.bin");
            numOfDJS = disjointedSets.size();
            setTitle("Project 3");
            setSize(400, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new FlowLayout());

            initComponents();
            addComponentsToFrame();

            setVisible(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void initComponents() {
        nameTextField = new JTextField(20);
        submitButton = new JButton("Submit");
        disjointedSetsLabel = new JLabel("Number of disjointed sets: " + numOfDJS);
        displayLabel = new JTextArea();

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String businessName = nameTextField.getText();
                String path = getPath(businessName);
                displayLabel.setText(path);
            }
        });
    }

    private void addComponentsToFrame() {
        add(new JLabel("Enter Business name:"));
        JScrollPane scrollPane = new JScrollPane(displayLabel);
        add(nameTextField);
        add(submitButton);
        add(disjointedSetsLabel);
        add(displayLabel);
        add(scrollPane,BorderLayout.CENTER);
    }

    private String getPath(String name) {
        String shortestPath = "";
        int shortPathLength = Integer.MAX_VALUE;
        ArrayList<String> clusterNames = new ArrayList<>();
        clusterNames.add("Chariot Pizza");
        clusterNames.add("New Aston Palace");
        clusterNames.add("Gandolfo's New York Deli - Delaware");
        clusterNames.add("OCF Coffee House");
        clusterNames.add("Taqueria Express");
        clusterNames.add("Townsend");

        for(String clusterName : clusterNames) {
            if (graph.dijkstra(graph.getNode(name).num, graph.getNode(clusterName).num) != null) {
                String[] path = graph.dijkstra(graph.getNode(name).num, graph.getNode(clusterName).num).split(" -> ");
                if (path.length < shortPathLength) {
                    shortestPath = graph.dijkstra(graph.getNode(name).num, graph.getNode(clusterName).num);
                    shortPathLength = path.length;
                }
            }
        }
        String[] names = shortestPath.split(" -> ");
        StringBuilder formattedPath = new StringBuilder();
        for (String businessName : names) {
            formattedPath.append(businessName).append("\n");
        }
        return formattedPath.toString();
    }

    public static void main(String[] args) {
        Application app = new Application();



    }

    public static BusinessGraph deserializeGraph(String filePath) {
        BusinessGraph businessGraph = null;
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            businessGraph = (BusinessGraph) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error while deserializing BusinessGraph object: " + e.getMessage());
        }
        return businessGraph;
    }


    public static List<List<String>> deserializeDJS(String filePath) {
        List<List<String>> djs = null;
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            djs = (List<List<String>>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error while deserializing BusinessGraph object: " + e.getMessage());
        }
        return djs;
    }
}