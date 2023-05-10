package NeuralNetworking.Change;

import java.io.Serializable;

public abstract class Change implements Serializable {
    private double val;
    private int layerPos;
    private int nodePos;

    public Change(double val, int layerPos, int nodePos) {
        this.val = val;
        this.layerPos = layerPos;
        this.nodePos = nodePos;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public void setLayerPos(int layerPos) {
        this.layerPos = layerPos;
    }

    public void setNodePos(int nodePos) {
        this.nodePos = nodePos;
    }

    public double getVal() {
        return val;
    }

    public int getLayerPos() {
        return layerPos;
    }

    public int getNodePos() {
        return nodePos;
    }
}
