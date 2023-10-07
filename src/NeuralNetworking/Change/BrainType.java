package NeuralNetworking.Change;

import Jama.Matrix;
import Jama.QRDecomposition;
import NeuralNetworking.Brain;
import NeuralNetworking.Brain2;
import NeuralNetworking.BrainObject;
import NeuralNetworking.BrainThread;
import NeuralNetworking.Nodes.Connection;
import NeuralNetworking.Nodes.Input;
import NeuralNetworking.Nodes.Node;
import NeuralNetworking.Nodes.Output;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public interface BrainType {
    public void applyBrain(BrainType b);
    public void randomize();
    public void update();
    public static void start(LinkedList<BrainObject> objects){
        if (objects.size()>0){
            if (objects.getFirst().getBrain() instanceof Brain)
                Brain.start(objects);
            else
                Brain2.start(objects);
        }
    }

    public static BrainType evolve(LinkedList<BrainObject> objects,double n){
        if (objects.size()>0){
            if (objects.getFirst().getBrain() instanceof Brain)
                return Brain.evolve(objects, n);
            else
                return Brain2.evolve(objects, n);
        }
        return null;
    }

    public static BrainType evolve2(LinkedList<BrainObject> objects){
        if (objects.size()>0){
            if (objects.getFirst().getBrain() instanceof Brain)
                return Brain.evolve2(objects);
            else
                return Brain2.evolve2(objects);
        }
        return null;
    }
}
