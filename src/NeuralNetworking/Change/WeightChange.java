package NeuralNetworking.Change;

public class WeightChange extends Change{
    private int connectionPos;
    public WeightChange(double val, int layerPos, int nodePos, int connectionPos) {
        super(val, layerPos, nodePos);
        this.connectionPos = connectionPos;
    }

    public int getConnectionPos() {
        return connectionPos;
    }

    public void setConnectionPos(int connectionPos) {
        this.connectionPos = connectionPos;
    }
}
