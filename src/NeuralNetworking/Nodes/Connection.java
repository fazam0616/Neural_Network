package NeuralNetworking.Nodes;

import java.io.Serializable;

public class Connection implements Serializable {
    private Node parent;
    private Node child;
    private double weight;

    public Connection(double weight, Node parent, Node child) {
        this.parent = parent;
        this.child = child;
        this.weight = weight;
    }

    public Node getParent() {
        return parent;
    }

    public Node getChild() {
        return child;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
