package NeuralNetworking;

import Jama.Matrix;
import Jama.QRDecomposition;
import NeuralNetworking.Change.BrainType;
import NeuralNetworking.Change.Change;
import NeuralNetworking.Change.WeightChange;
import NeuralNetworking.Nodes.Connection;
import NeuralNetworking.Nodes.Input;
import NeuralNetworking.Nodes.Node;
import NeuralNetworking.Nodes.Output;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Brain implements Serializable, BrainType {
    private Node[][] layers;
    private Change change;
    public Brain(Input[] inputLayer, int hiddenLayerCount, int hiddenLayerSize, Output[] outputLayer){
        layers = new Node[hiddenLayerCount+2][hiddenLayerSize];
        layers[0] = inputLayer;
        layers[layers.length-1] = outputLayer;

        for (int i = 1; i < layers.length-1; i++) {
            for (int j = 0; j < layers[i].length; j++) {
                layers[i][j] = new Node();
            }
        }
        int nodeCount = 0;
        for (int i = layers.length-1; i > 0; i--)
            for (int j = 0; j < layers[i].length; j++)
                for (int k = 0; k < layers[i - 1].length; k++){
                    layers[i][j].addNode(0,layers[i-1][k]);
                    nodeCount++;
                }
        //System.out.println(nodeCount*hiddenLayerSize*8);
    }
    //({6.0764101506668835,5.186463528692611,-1.1143764128869569},{5.141913836368177,-3.3271683493025677,-2.341500083997435}),({4.616472768631368,4.813948901386801},{-4.37902181191741,-4.6337938134514625}),({8.289072083159363,3.938348718377254},{5.81960339701706,6.192164241732041})
    public Brain(Input[] inputLayer, Output[] outputLayer, String weights){
        int hiddenLayerCount = weights.length() - weights.replaceAll("<","").length();
        int currentLayer = 0;
        int currentNode = -1;
        int connectionNum = 0;
        layers = new Node[hiddenLayerCount+1][1];
        layers[0] = inputLayer;
        layers[layers.length-1] = outputLayer;
        Scanner s = new Scanner(weights);
        char c;

        s.useDelimiter("");
        while(s.hasNext()){
            if(s.hasNextDouble()){
                double num = s.nextDouble();
                layers[currentLayer][currentNode].addNode(num,layers[currentLayer-1][connectionNum++]);
                //System.out.print(num+" ");
                if (!s.hasNextDouble())
                    s.useDelimiter("");
            }
            else{
                c = s.next().charAt(0);
                //System.out.print(c);
                switch (c){
                    case '<':
                        s.useDelimiter("\\{");
                        int layerSize = s.nextInt();
                        s.useDelimiter("");

                        currentLayer++;
                        if (currentLayer<layers.length-1){
                            layers[currentLayer] = new Node[layerSize];
                            for (int j = 0; j < layerSize; j++) {
                                layers[currentLayer][j] = new Node();
                            }
                        }
                        currentNode = -1;
                        break;
                    case '{':
                        currentNode++;
                        connectionNum = 0;
                        s.useDelimiter(",");
                        break;
                }
            }
        }
        //System.out.println();
    }

    public static Brain evolve(LinkedList<BrainObject> objects,double n){
        Brain master;

        Random r = new Random();

        BrainObject.sort(objects);

        master = (Brain)objects.getLast().getBrain();
        System.out.println(objects.getLast().fitnessReport());

        LinkedList<Output> group = new LinkedList<>();
        LinkedList<BrainThread> threads = new LinkedList<>();
        for (int i = 0; i<objects.size()-2; i++) {
            int finalI = i;
            BrainObject object = objects.get(finalI);
            object.reset();

            group.add(new Output((double x)->{
                if ((double)finalI/objects.size() < 0.8){
                    for (int layerPos = 0; layerPos < master.layers.length; layerPos++) {
                        for (int nodePos = 0; nodePos < master.layers[layerPos].length; nodePos++) {
                            for (int connPos = 0; connPos < master.layers[layerPos][nodePos].getConnections().size(); connPos++) {
                                double changeVal = master.layers[layerPos][nodePos].getConnections().get(connPos).getWeight()+(5*(1+n/10))*r.nextGaussian();
                                Change c = new WeightChange(changeVal,layerPos,nodePos,connPos);

                                ((Brain)((Brain)object.getBrain())).layers[layerPos][nodePos].getConnections().get(connPos).setWeight(changeVal);
                            }
                        }
                    }
                } else{
                    ((Brain)object.getBrain()).randomize();
                }
            }));
            if (group.size() > objects.size()/78){
                threads.add(new BrainThread(group));
                threads.getLast().start();
                group = new LinkedList<>();
            }
        }
        threads.add(new BrainThread(group));
        threads.getLast().start();

        for (BrainThread t:threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        objects.getLast().reset();
        objects.getLast().isBest = true;

        return master;
    }

    public static Brain evolve2(LinkedList<BrainObject> objects){
        double[][] scores = new double[objects.size()][];
        double[][] param = new double[objects.size()][];

        System.out.println("-Reading Data...");
        LinkedList<Output> group = new LinkedList<>();
        LinkedList<BrainThread> threads = new LinkedList<>();
        for (int k = 0; k<objects.size(); k++){
            int finalK = k;
            LinkedList<BrainObject> finalObjects = objects;
            group.add(new Output((double x) -> {
                BrainObject object = finalObjects.get(finalK);
                LinkedList<Double> weights = new LinkedList<>();
                for (int i = 0; i < ((Brain)((Brain)object.getBrain())).layers.length; i++) {
                    for (Node n:((Brain)((Brain)object.getBrain())).layers[i]) {
                        for(Connection con:n.getConnections())
                            weights.add(con.getWeight());
                    }
                }

                param[finalK] = new double[weights.size()];
                for (int i = 0; i < param[finalK].length; i++) {
                    param[finalK][i] = weights.get(i);
                }
                scores[finalK] = new double[]{object.getFitness()};
            }));
            if (group.size() > objects.size()/78){
                threads.add(new BrainThread(group));
                threads.getLast().start();
                group = new LinkedList<>();
            }
        }
        threads.add(new BrainThread(group));
        threads.getLast().start();

        for (BrainThread t:threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        System.out.println("-Dumping Data...");
//        File f = new File("RAM_DUMP.temp");
//        FileOutputStream w = new FileOutputStream(f);
//        ObjectOutputStream o = new ObjectOutputStream(w);
//
//        for (int i = 0; i < objects.size(); i++) {
//            o.writeObject(objects.get(i).getBrain());
//            objects.get(i).setBrain(null);
//            o.flush();
//            w.flush();
//            if (i%(objects.size()/20)==0){
//                Runtime.getRuntime().gc();
//                System.out.println("-- "+Math.round(100.0*i/objects.size())+"% Done: "+Runtime.getRuntime().freeMemory()/1024+"MB free");
//            }
//        }
//        objects = new LinkedList<>();
//        o.close();
//        w.close();

        System.out.println("--Calculating QR Decomposition...");

        QRDecomposition q = new QRDecomposition(new Matrix(param));
        Matrix m = q.solve(new Matrix(scores));
        double[][] b = m.getArray();
        double len = 0;
        for (double[] doubles : b) {
            len += Math.pow(doubles[0], 2);
        }
        len = 1/Math.sqrt(len);
        q = null;
        m = null;

//        System.out.println("-Reading In Dump Data");
//        FileInputStream in = new FileInputStream(f);
//        ObjectInputStream u = new ObjectInputStream(in);
//        int n = 0;
//        while (u.available() > 0){
//            objects.get(n++).setBrain((Brain) u.readObject());
//        }
//        u.close();
//        in.close();

        Random r = new Random();

        BrainObject.sort(objects);
        Brain basis = (Brain)objects.getLast().getBrain();

        System.out.println(objects.getLast().fitnessReport());



        System.out.println("--Applying QR Decomposition...");

        group = new LinkedList<>();
        threads = new LinkedList<>();
        for (int i = 0; i<objects.size(); i++) {
            BrainObject object = objects.get(i);
            object.reset();

            int finalI = i;
            double finalLen = len;
            LinkedList<BrainObject> finalObjects1 = objects;
            group.add(new Output((double x) -> {
                for (int layerPos = 0; layerPos < ((Brain)object.getBrain()).layers.length; layerPos++) {
                    for (int nodePos = 0; nodePos < ((Brain)object.getBrain()).layers[layerPos].length; nodePos++) {
                        for (int connPos = 0; connPos < ((Brain)object.getBrain()).layers[layerPos][nodePos].getConnections().size(); connPos++) {
                            int pNum = 0;
                            for (int j = 0; j < layerPos; j++) {
                                pNum += ((Brain)object.getBrain()).layers[layerPos].length;
                            }
                            for (int j = 0; j < nodePos; j++) {
                                pNum += ((Brain)object.getBrain()).layers[layerPos][nodePos].getConnections().size();
                            }

                            pNum += connPos;
                            double changeVal=((Brain)object.getBrain()).layers[layerPos][nodePos].getConnections().get(connPos).getWeight();
                            if (finalI != finalObjects1.size()-1){
                                if ((double)finalI/finalObjects1.size() > 0.95)
                                    if(r.nextDouble() > 0.95)
                                        changeVal = 15*r.nextGaussian();
                                else if ((double)finalI/finalObjects1.size() > 0.8)
                                    if(r.nextDouble() > 0.5)
                                        changeVal = basis.layers[layerPos][nodePos].getConnections().get(connPos).getWeight() + finalLen*b[pNum][0] + 5*r.nextGaussian();
                                else
                                    changeVal = basis.layers[layerPos][nodePos].getConnections().get(connPos).getWeight() + finalLen*b[pNum][0];
                                Change c = new WeightChange(changeVal,layerPos,nodePos,connPos);

                                ((Brain)object.getBrain()).applyChange(c);
                            }else
                                object.isBest = true;
                        }
                    }
                }
            }));
            if (group.size() > objects.size()/78){
                threads.add(new BrainThread(group));
                threads.getLast().start();
                group = new LinkedList<>();
            }
        }
        threads.add(new BrainThread(group));
        threads.getLast().start();

        for (BrainThread t:threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



        return basis;
    }

    public static void start(LinkedList<BrainObject> objects){
        Brain master;
        double changeVal;
        Change c;
        Random r = new Random();

        objects.get(0).getBrain().randomize();
        master = (Brain)objects.get(0).getBrain();
        for (BrainObject object : objects) {
            for (int layerPos = 0; layerPos < master.layers.length; layerPos++) {
                for (int nodePos = 0; nodePos < master.layers[layerPos].length; nodePos++) {
                    for (int connPos = 0; connPos < master.layers[layerPos][nodePos].getConnections().size(); connPos++) {
                        changeVal = r.nextGaussian()*5 + ((Brain)object.getBrain()).layers[layerPos][nodePos].getConnections().get(connPos).getWeight();
                        c = new WeightChange(changeVal,layerPos,nodePos,connPos);

                        ((Brain)object.getBrain()).setChange(c);
                        ((Brain)object.getBrain()).applyChange(c);
                    }
                }
            }
        }
    }

    public static void start(LinkedList<BrainObject> objects, Brain master){
        double changeVal;
        Change c;
        Random r = new Random();

        for (int i = 0; i<objects.size();i++) {
            BrainObject object = objects.get(i);
            for (int layerPos = 0; layerPos < master.layers.length; layerPos++) {
                for (int nodePos = 0; nodePos < master.layers[layerPos].length; nodePos++) {
                    for (int connPos = 0; connPos < master.layers[layerPos][nodePos].getConnections().size(); connPos++) {
                        if(i!=objects.size()-1){
                            changeVal = r.nextGaussian() + master.layers[layerPos][nodePos].getConnections().get(connPos).getWeight();

                        }else{
                            changeVal = master.layers[layerPos][nodePos].getConnections().get(connPos).getWeight();
                        }
                        c = new WeightChange(changeVal,layerPos,nodePos,connPos);

                        ((Brain)object.getBrain()).setChange(c);
                        ((Brain)object.getBrain()).applyChange(c);
                    }
                }
            }
        }
        objects.getLast().isBest = true;
    }

    public void applyBrain(BrainType b){
        for (int j = 0; j < this.getLayers().length; j++) {
            for (int k = 0; k < this.getLayers()[j].length; k++) {
                for (int l = 0; l < this.getLayers()[j][k].getConnections().size(); l++) {
                    Connection c = this.getLayers()[j][k].getConnections().get(l);
                    Connection that = ((Brain)b).getLayers()[j][k].getConnections().get(l);
                    c.setWeight(that.getWeight());
                }
            }
        }
    }

    public void randomize(){
        Random r = new Random();
        double val;
        for (int j = 0; j < this.getLayers().length; j++) {
            for (int k = 0; k < this.getLayers()[j].length; k++) {
                for (int l = 0; l < this.getLayers()[j][k].getConnections().size(); l++) {
                    Connection c = this.getLayers()[j][k].getConnections().get(l);
                    val = r.nextGaussian()+r.nextGaussian()*5;
                    c.setWeight(val);
                }
            }
        }
    }

    @Override
    public Brain clone(){
        Input[] i = new Input[this.layers[0].length];
        Output[] o = new Output[this.layers[this.layers.length-1].length];
        Brain b;
        Arrays.fill(i,new Input(null));
        Arrays.fill(o,new Output(null));

        b = new Brain(i,this.layers.length-2,this.layers[1].length,o);

        for (int j = 0; j < b.getLayers().length; j++) {
            for (int k = 0; k < b.getLayers()[j].length; k++) {
                for (int l = 0; l < b.getLayers()[j][k].getConnections().size(); l++) {
                    b.getLayers()[j][k].getConnections().get(l).setWeight(
                            this.getLayers()[j][k].getConnections().get(l).getWeight()
                    );
                }
            }
        }

        return b;
    }

    public void update() {
        for (Node[] layer:layers)
            for(Node n:layer)
                n.updated = true;
        for(Node o:layers[layers.length-1]){
            ((Output)o).run();
        }
    }

    public void applyChange(Change c){
        double oldPos;
        Node n = this.layers[c.getLayerPos()][c.getNodePos()];
        if (c instanceof WeightChange){
            n.getConnections().get(((WeightChange)c).getConnectionPos()).setWeight(c.getVal());
        }
    }

    public Node[][] getLayers() {
        return layers;
    }

    public Change getChange() {
        return change;
    }

    public void setChange(Change change) {
        this.change = change;
    }

    @Override
    public String toString(){
        String out = "";
        for (int i = 1; i < layers.length; i++) {
            out += "<";
            out += layers[i].length;
            for (Node n:layers[i]) {
                out += "{";
                for(Connection c:n.getConnections()){
                    out += c.getWeight()+",";
                }

                out += "}";
            }
            out += ">";
        }

        return out;
    }
}

