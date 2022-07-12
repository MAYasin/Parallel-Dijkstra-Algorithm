package com.multiagent.datastructure;

public class Edge<T> {
    private int cost = 0;
    private Vertex<T> v1, v2 = null;

    public Edge(int cost, Vertex<T> v1, Vertex<T> v2) {
        this.cost = cost;
        this.v1 = v1;
        this.v2 = v2;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public Vertex<T> getV1() {
        return v1;
    }

    public void setV1(Vertex<T> v1) {
        this.v1 = v1;
    }

    public Vertex<T> getV2() {
        return v2;
    }

    public void setV2(Vertex<T> v2) {
        this.v2 = v2;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(v1);
        sb.append("<-"+cost+"->");
        sb.append(v2);
        return sb.toString();
    }
}
