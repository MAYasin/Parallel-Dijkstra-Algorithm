package com.multiagent.datastructure;

import java.util.ArrayList;
import java.util.List;

public class Vertex<T> {
    private List<Edge<T>> connectedEdges = new ArrayList<>();
    private T value = null;

    public Vertex(T value) {
        this.value = value;
    }

    public Vertex(T value, int weight) {
        this.value = value;
    }

    public List<Edge<T>> getConnectedEdges() {
        return connectedEdges;
    }

    public void setConnectedEdges(Edge<T> e) {
        this.connectedEdges.add(e);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(value);
        return sb.toString();
    }

    @Override
    public boolean equals(Object v1) {
        if (!(v1 instanceof Vertex)){
            return false;
        }

        final Vertex<T> v = (Vertex<T>) v1;

        final boolean valuesEquals = this.value.equals(v.value);
        if (!valuesEquals){
            return false;
        }

        return true;
    }

}
