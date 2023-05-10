package NeuralNetworking.Nodes;

public class Output extends Node{
    private OutputLambda output;
    public Output(OutputLambda o) {
        super();
        output = o;
    }

    public void run(){
        output.run(this.getValue());
    }


    public OutputLambda getOutput() {
        return output;
    }

    public void setOutput(OutputLambda output) {
        this.output = output;
    }
}
