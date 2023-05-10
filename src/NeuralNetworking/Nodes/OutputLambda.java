package NeuralNetworking.Nodes;

import java.io.Serializable;

public interface OutputLambda extends Serializable {
    public abstract void run(double x);
}
