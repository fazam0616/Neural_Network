package NeuralNetworking.Change;

import Jama.Matrix;
import Jama.QRDecomposition;
import NeuralNetworking.Brain;
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
    public void applyBrain(Brain b);
    public void randomize();
    public void update();
}
