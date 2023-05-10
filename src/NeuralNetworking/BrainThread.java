package NeuralNetworking;

import NeuralNetworking.Nodes.Output;

import java.util.LinkedList;

public class BrainThread extends Thread{
    public LinkedList<Output> o;

    public BrainThread(LinkedList<Output> o) {
        this.o = o;
    }

    @Override
    public void run(){
        for(Output n:o)
            n.run();
    }
}
