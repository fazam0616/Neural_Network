package NeuralNetworking.Nodes;

import java.io.Serializable;
import java.util.LinkedList;

public class Node implements Serializable {
    private LinkedList<Connection> connections = new LinkedList<>();
    public boolean updated = false;
    private double currentVal;

    public Node(){}

    public void addNode(double weight, Node child){
        this.connections.add(new Connection(weight, this,child));
    }

    public double getValue() {
        if (updated){
            double val = 0;
            for (Connection c : connections) {
                val += c.getChild().getValue() * c.getWeight();
            }
            currentVal = val;
            updated = false;
            return val;
        } else {
            return currentVal;
        }
    }

    public LinkedList<Connection> getConnections() {
        return connections;
    }
}
