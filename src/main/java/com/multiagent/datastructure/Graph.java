package com.multiagent.datastructure;

import java.util.*;


public class Graph<T> {

    private List<Vertex<T>> visited = new ArrayList<>();
    private List<Vertex<T>> unvisited = new ArrayList<>();

    private Map<Vertex<T>, Integer> shortestDistance = new HashMap<Vertex<T>, Integer>();
    private Map<Vertex<T>, Vertex<T>> prevVertex = new HashMap<Vertex<T>, Vertex<T>>();

    private List<Vertex<T>> allVertices = new ArrayList<Vertex<T>>();
    private List<Edge<T>> allEdges = new ArrayList<Edge<T>>();

    public List<Vertex<T>> getVertices() {
        return allVertices;
    }

    public List<Edge<T>> getEdges() {
        return allEdges;
    }

    public Graph() {}

    public Graph(List<Vertex<T>> vertices, List<Edge<T>> edges) {
        this.allVertices.addAll(vertices);
        this.allEdges.addAll(edges);

        for (Edge<T> e : edges) {
            final Vertex<T> from = e.getV1();
            final Vertex<T> to = e.getV2();

            from.setConnectedEdges(e);

            Edge<T> copy = new Edge<T>(e.getCost(), from, to);
            to.setConnectedEdges(copy);
            this.allEdges.add(copy);
        }
    }

    public Map<Vertex<T>, Integer> getShortestDistance() {
        return shortestDistance;
    }

    public Vertex<T> getVertex(T tVal){
        Vertex<T> val = null;
        for (Vertex<T> v:allVertices) {
            if(v.getValue().equals(tVal)){
                val = v;
            }
        }
        return val;
    }

    public int getVertexIndex(T tVal){
        int pos = 0;
        int index = 0;
        for (Vertex<T> v:allVertices) {
            if(v.getValue().equals(tVal)){
                pos = index;
            }
            index++;
        }
        return pos;
    }

    public ArrayList<Edge<T>> getAdjacentEdges(Vertex<T> v) {
        ArrayList<Edge<T>> adj = new ArrayList<Edge<T>>();
        for (Edge<T> e : allEdges) {
            if (e.getV1() == v || e.getV2() == v) {
                adj.add(e);
            }
        }
        return adj;
    }

    public void Dijkstra(Vertex<T> source) {
        shortestDistance.clear();
        visited.clear();
        unvisited.clear();

        for (Vertex<T> v : allVertices) {
            shortestDistance.put(v, Integer.MAX_VALUE);
        }
        shortestDistance.put(source, 0);
        unvisited.addAll(allVertices);

        for (Edge<T> edge : source.getConnectedEdges()) {
            int dist = shortestDistance.get(source) + edge.getCost();
            if(edge.getV1() != source) {
                if (dist < shortestDistance.get(edge.getV1())) {
                    shortestDistance.put(edge.getV1(), dist);
                    prevVertex.put(edge.getV1(), source);
                }
            }else {
                if (dist < shortestDistance.get(edge.getV2())) {
                    shortestDistance.put(edge.getV2(), dist);
                    prevVertex.put(edge.getV2(), source);
                }
            }
        }
        visited.add(source);
        unvisited.remove(source);

        List<Vertex<T>> tempVertex = new ArrayList<Vertex<T>>();
        for (Vertex<T> v1 : unvisited) {
            tempVertex.add(v1);
        }

        for (int i = 0; i < tempVertex.size(); i++) {

            Map<Vertex<T>, Integer> templist = new HashMap<Vertex<T>, Integer>();

            for (Vertex<T> v1 : unvisited) {
                templist.put(v1, shortestDistance.get(v1));
            }

            templist = sortByValue(templist);

            Vertex<T> v2 = (Vertex<T>) templist.keySet().toArray()[0];

            List<Edge<T>> e1 = new ArrayList<Edge<T>>();

            for (Edge<T> e : getAdjacentEdges(v2)) {
                if (!(visited.contains(e.getV1()) || visited.contains(e.getV2()))) {
                    if (!e1.toString().contains(e.toString())) {
                        e1.add(e);
                    }
                }
            }

            for (Edge<T> edge : e1) {
                int dist = shortestDistance.get(v2) + edge.getCost();
                if(edge.getV1() != v2) {
                    if (dist < shortestDistance.get(edge.getV1())) {
                        shortestDistance.put(edge.getV1(), dist);
                        prevVertex.put(edge.getV1(), v2);
                    }
                }else {
                    if (dist < shortestDistance.get(edge.getV2())) {
                        shortestDistance.put(edge.getV2(), dist);
                        prevVertex.put(edge.getV2(), v2);
                    }
                }
            }

            visited.add(v2);
            unvisited.remove(v2);
        }
    }

    public LinkedList<Vertex<T>> getPath(Vertex<T> destination) {
        LinkedList<Vertex<T>> path = new LinkedList<Vertex<T>>();
        Vertex<T> pos = destination;
        path.add(pos);
        while (prevVertex.get(pos) != null) {
            pos = prevVertex.get(pos);
            path.add(pos);
        }
        Collections.reverse(path);
        return path;
    }

    public HashMap<Vertex<T>, Integer> sortByValue(Map<Vertex<T>, Integer> iMap) {
        List<Map.Entry<Vertex<T>, Integer>> list = new LinkedList<Map.Entry<Vertex<T>, Integer>>(iMap.entrySet());
        Collections.sort(list, Comparator.comparing(Map.Entry::getValue));
        HashMap<Vertex<T>, Integer> tempMap = new LinkedHashMap<Vertex<T>, Integer>();
        for (Map.Entry<Vertex<T>, Integer> val : list) {
            tempMap.put(val.getKey(), val.getValue());
        }
        return tempMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(allVertices);
        sb.append("\n");
        sb.append(allEdges);
        return sb.toString();
    }
}
