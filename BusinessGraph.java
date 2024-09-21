import java.io.Serializable;
import java.util.*;

public class BusinessGraph implements Serializable {

    public ArrayList<LinkedList<GNode>> adjacencyList;
    public HashMap<String, GNode> added;
    public List<Edge> edges;
    public int size = 0;

    public BusinessGraph() {
        adjacencyList = new ArrayList<>();
        added = new HashMap<>();
        edges = new ArrayList<>();
    }

    public GNode getNode(String name) {
        return added.get(name);
    }

    public void addNode(GNode node) {
        LinkedList<GNode> currentList = new LinkedList<>();
        currentList.add(node);
        adjacencyList.add(currentList);
        node.num = size;
        added.put(node.business.name, node);
        size++;
    }

    public void addEdge(int src, int dst, double weight) {
        LinkedList<GNode> currentList = adjacencyList.get(src);
        GNode destNode = adjacencyList.get(dst).get(0);
        currentList.add(destNode);
        edges.add(new Edge(src, dst, weight));
    }

    public List<List<String>> getDisjointSets() {
        UnionFind uf = new UnionFind(size);
        for (int i = 0; i < adjacencyList.size(); i++) {
            LinkedList<GNode> currentList = adjacencyList.get(i);
            GNode srcNode = currentList.get(0);

            for (int j = 1; j < currentList.size(); j++) {
                GNode destNode = currentList.get(j);
                uf.union(srcNode.num, destNode.num);
            }
        }

        Map<Integer, List<String>> disjointSetsMap = new HashMap<>();
        for (LinkedList<GNode> nodeList : adjacencyList) {
            GNode currentNode = nodeList.get(0);
            int root = uf.find(currentNode.num);

            disjointSetsMap.putIfAbsent(root, new ArrayList<>());
            disjointSetsMap.get(root).add(currentNode.business.name);
        }

        return new ArrayList<>(disjointSetsMap.values());
    }


    public void printDisjointSets(List<List<String>> disjointSets) {
        int setCount = 1;
        for (List<String> set : disjointSets) {
            if (set.size() > 1) {
                System.out.println("-----------------------------------------------------------------------------");
                System.out.println("Disjoint Set " + setCount + ":");
                for (String business : set) {
                    System.out.print(business + " -> ");
                }
                System.out.println();
            }
            setCount++;
        }
    }

    public void printGraph(){
        for(LinkedList<GNode> currentList : adjacencyList) {
            for(GNode node : currentList) {
                System.out.print(node.business.name + " -> ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // Inside ALGraph class

    public boolean areNodesInSameSet(int node1, int node2) {
        UnionFind uf = new UnionFind(size);

        for (int i = 0; i < adjacencyList.size(); i++) {
            LinkedList<GNode> currentList = adjacencyList.get(i);
            GNode srcNode = currentList.get(0);

            for (int j = 1; j < currentList.size(); j++) {
                GNode destNode = currentList.get(j);
                uf.union(srcNode.num, destNode.num);
            }
        }

        return uf.find(node1) == uf.find(node2);
    }

    public String checkAndFindPath(int src, int dst) {
        if (areNodesInSameSet(src, dst)) {
            return dijkstra(src, dst);
        } else {
            return null;
        }
    }

    public double getEdgeWeight(int src, int dst) {
        for (Edge edge : edges) {
            if (edge.src == src && edge.dst == dst) {
                return edge.weight;
            }
        }
        return Integer.MAX_VALUE;
    }


    public String dijkstra(int src, int dst) {
        double[] dist = new double[size];
        boolean[] visited = new boolean[size];
        int[] prev = new int[size];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[src] = 0;

        for (int i = 0; i < size; i++) {
            int u = -1;
            for (int j = 0; j < size; j++) {
                if (!visited[j] && (u == -1 || dist[j] < dist[u])) {
                    u = j;
                }
            }

            if (dist[u] == Integer.MAX_VALUE) {
                break;
            }

            visited[u] = true;

            LinkedList<GNode> neighbors = adjacencyList.get(u);
            for (int j = 1; j < neighbors.size(); j++) {
                GNode neighbor = neighbors.get(j);
                double weight = getEdgeWeight(u, neighbor.num);
                if (dist[u] + weight < dist[neighbor.num]) {
                    dist[neighbor.num] = dist[u] + weight;
                    prev[neighbor.num] = u;
                }
            }
        }

        if (dist[dst] == Integer.MAX_VALUE) {
            return null; // No path found
        }

        List<String> path = new ArrayList<>();
        String pathString = "";
        for (int node = dst; node != -1; node = prev[node]) {
            path.add(adjacencyList.get(node).get(0).business.name);
        }
        Collections.reverse(path);
        for (int i = 0; i < path.size(); i++) {
            String data = path.get(i);
            if (i == path.size()-1) {
                pathString = pathString.concat(data);
            } else {
                pathString = pathString.concat(data + " -> ");
            }
        }
        return pathString;
    }

    public void printDJS(List<List<Character>> djs) {
        int setCount = 1;
        for (List<Character> set : djs) {
            if (set.size() > 1) {
                System.out.println("-----------------------------------------------------------------------------");
                System.out.println("Disjoint Set " + setCount + ":");
                for (Character data : set) {
                    System.out.print(data + " -> ");
                }
                System.out.println();
            }
            setCount++;
        }
    }

    public List<String> getSetOfNode(int node) {
        UnionFind uf = new UnionFind(size);

        for (int i = 0; i < adjacencyList.size(); i++) {
            LinkedList<GNode> currentList = adjacencyList.get(i);
            GNode srcNode = currentList.get(0);

            for (int j = 1; j < currentList.size(); j++) {
                GNode destNode = currentList.get(j);
                uf.union(srcNode.num, destNode.num);
            }
        }

        int root = uf.find(node);
        List<String> nodeSet = new ArrayList<>();

        for (LinkedList<GNode> nodeList : adjacencyList) {
            GNode currentNode = nodeList.get(0);
            if (uf.find(currentNode.num) == root) {
                nodeSet.add(currentNode.business.name);
            }
        }
        return nodeSet;
    }
}