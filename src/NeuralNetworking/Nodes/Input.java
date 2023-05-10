package NeuralNetworking.Nodes;

public class Input extends Node{
    private InputLambda input;
    public Input(InputLambda i) {
        super();
        input = i;
    }

    public double getValue(){
        return input.run();
    }

    public void setInput(InputLambda input) {
        this.input = input;
    }

    public InputLambda getInput() {
        return input;
    }
}
