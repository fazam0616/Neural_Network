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

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class Brain2 implements Serializable, BrainType {
    public Matrix[] weights;
    Input[] inputs;
    Output[] outputs;
    public Brain2(Input[] inputLayer, int hiddenLayerCount, int hiddenLayerSize, Output[] outputLayer){
        inputs = inputLayer;
        outputs = outputLayer;
        weights = new Matrix[hiddenLayerCount+2];
        weights[0] = new Matrix(inputLayer.length,hiddenLayerSize);
        weights[hiddenLayerCount+1] = new Matrix(hiddenLayerSize,outputLayer.length);
        for (int i = 1; i <= hiddenLayerCount; i++) {
            weights[i] = new Matrix(this.weights[i-1].getColumnDimension(),hiddenLayerSize);
        }
    }
    //({6.0764101506668835,5.186463528692611,-1.1143764128869569},{5.141913836368177,-3.3271683493025677,-2.341500083997435}),({4.616472768631368,4.813948901386801},{-4.37902181191741,-4.6337938134514625}),({8.289072083159363,3.938348718377254},{5.81960339701706,6.192164241732041})
    public Brain2(Input[] inputLayer, Output[] outputLayer, String data){
        int hiddenLayerCount = data.length() - data.replaceAll("<","").length();
        int currentLayer = 0;
        int currentNode = -1;
        int connectionNum = 0;
        inputs = inputLayer;
        outputs = outputLayer;

        Scanner s = new Scanner(data);
        char c;

        weights = new Matrix[hiddenLayerCount+2];

        weights[0] = new Matrix(inputLayer.length,hiddenLayerCount);
        weights[hiddenLayerCount+1] = new Matrix(hiddenLayerCount,outputLayer.length);
        for (int i = 1; i <= hiddenLayerCount; i++) {
            weights[i] = new Matrix(this.weights[i-1].getColumnDimension(),hiddenLayerCount);
        }

        s.useDelimiter("");
        while(s.hasNext()){
            if(s.hasNextDouble()){
                double num = s.nextDouble();
                weights[currentLayer].set(currentNode,connectionNum++,num);
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
                        //weights[0] = new Matrix(inputLayer.length,layerSize);
                        s.useDelimiter("");

                        if (currentLayer == 0){
                            weights[currentLayer] = new Matrix(inputLayer.length,layerSize);
                        }else if (currentLayer<hiddenLayerCount+2){
                            weights[currentLayer] = new Matrix(weights[currentLayer-1].getColumnDimension(),layerSize);
                        }else{
                            weights[currentLayer] = new Matrix(layerSize,outputLayer.length);
                        }
                        currentLayer++;
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

    public static Brain2 evolve(LinkedList<BrainObject> objects, double n){
        Brain2 master;

        Random r = new Random();

        BrainObject.sort(objects);

        master = (Brain2)objects.getLast().getBrain();
        System.out.println(objects.getLast().fitnessReport());

        LinkedList<Output> group = new LinkedList<>();
        LinkedList<BrainThread> threads = new LinkedList<>();
        for (int i = 0; i<objects.size()-2; i++) {
            int finalI = i;
            BrainObject object = objects.get(finalI);
            object.reset();

            group.add(new Output((double x)->{
                if ((double)finalI/objects.size() < 0.8){
                    for (int layerPos = 0; layerPos < master.weights.length; layerPos++) {
                        for (int nodePos = 0; nodePos < master.weights[layerPos].getRowDimension(); nodePos++) {
                            for (int connPos = 0; connPos < master.weights[layerPos].getColumnDimension(); connPos++) {
                                double changeVal = master.weights[layerPos].get(nodePos,connPos)+(5*(1+n/10))*r.nextGaussian();
                                //Change c = new WeightChange(changeVal,layerPos,nodePos,connPos);
                                ((Brain2)object.getBrain()).weights[layerPos].set(nodePos,connPos,changeVal);
                            }
                        }
                    }
                } else{
                    object.getBrain().randomize();
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

    public static Brain2 evolve2(LinkedList<BrainObject> objects){
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

                for (Matrix n:((Brain2)object.getBrain()).weights) {
                    for (int i = 0; i < n.getRowDimension(); i++) {
                        for(int j = 0; j < n.getColumnDimension();j++) {
                            weights.add(n.get(i, j));
                        }
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
        Brain2 basis = (Brain2) objects.getLast().getBrain();

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
                for (int layerPos = 0; layerPos < ((Brain2)object.getBrain()).weights.length; layerPos++) {
                    for (int nodePos = 0; nodePos < ((Brain2)object.getBrain()).weights[layerPos].getRowDimension(); nodePos++) {
                        for (int connPos = 0; connPos < ((Brain2)object.getBrain()).weights[layerPos].getColumnDimension(); connPos++) {
                            int pNum = 0;
                            for (int j = 0; j < layerPos; j++) {
                                pNum += ((Brain2)object.getBrain()).weights[layerPos].getRowDimension();
                            }
                            for (int j = 0; j < nodePos; j++) {
                                pNum += ((Brain2)object.getBrain()).weights[layerPos].getColumnDimension();
                            }

                            pNum += connPos;
                            double changeVal=((Brain2)object.getBrain()).weights[layerPos].get(nodePos,connPos);
                            if (finalI != finalObjects1.size()-1){
                                if ((double)finalI/finalObjects1.size() > 0.95)
                                    if(r.nextDouble() > 0.95)
                                        changeVal = 15*r.nextGaussian();
                                    else if ((double)finalI/finalObjects1.size() > 0.8)
                                        if(r.nextDouble() > 0.5)

                                            changeVal = basis.weights[layerPos].get(nodePos,connPos) + finalLen*b[pNum][0] + 5*r.nextGaussian();
                                        else
                                            changeVal = basis.weights[layerPos].get(nodePos,connPos) + finalLen*b[pNum][0];
                                Change c = new WeightChange(changeVal,layerPos,nodePos,connPos);
                                basis.weights[layerPos].set(nodePos,connPos,changeVal);
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
        Brain2 master;
        double changeVal;
        Random r = new Random();

        objects.get(0).getBrain().randomize();
        master = (Brain2)objects.get(0).getBrain();
        for (BrainObject object : objects) {
            for (int layerPos = 0; layerPos < master.weights.length; layerPos++) {
                for (int nodePos = 0; nodePos < master.weights[layerPos].getRowDimension(); nodePos++) {
                    for (int connPos = 0; connPos < master.weights[layerPos].getColumnDimension(); connPos++) {
                        changeVal = r.nextGaussian()*5 + ((Brain2)object.getBrain()).weights[layerPos].get(nodePos,connPos);
                        ((Brain2)object.getBrain()).weights[layerPos].set(nodePos,connPos,changeVal);
                    }
                }
            }
        }
    }

    public static void start(LinkedList<BrainObject> objects, Brain2 master){
        double changeVal;
        Random r = new Random();

        for (BrainObject object : objects) {
            for (int layerPos = 0; layerPos < master.weights.length; layerPos++) {
                for (int nodePos = 0; nodePos < master.weights[layerPos].getRowDimension(); nodePos++) {
                    for (int connPos = 0; connPos < master.weights[layerPos].getColumnDimension(); connPos++) {
                        changeVal = r.nextGaussian()*5 + ((Brain2)object.getBrain()).weights[layerPos].get(nodePos,connPos);
                        ((Brain2)object.getBrain()).weights[layerPos].set(nodePos,connPos,changeVal);
                    }
                }
            }
        }
    }

    public void applyBrain(BrainType b){
        this.weights = ((Brain2)b).weights.clone();
    }

    public void randomize(){
        Random r = new Random();
        double val;
        for (int j = 0; j < weights.length; j++) {
            for (int k = 0; k < weights[j].getRowDimension(); k++) {
                for (int l = 0; l < weights[j].getColumnDimension(); l++) {
                    val = r.nextGaussian()+r.nextGaussian()*5;
                    weights[j].set(k,l,val);
                }
            }
        }
    }

    @Override
    public Brain2 clone(){
        Brain2 n = new Brain2(inputs,weights.length-2,weights[1].getRowDimension(),outputs);
        n.weights = this.weights.clone();
        return n;
    }

    public void update() {
        Matrix m = new Matrix(1,inputs.length);
        Matrix c;
        for (int i = 0; i < inputs.length; i++) {
            m.set(0,i,inputs[i].getValue());
        }

        for (int i = 0; i < weights.length; i++) {
            m = m.times(weights[i]);
        }

        for (int i = 0; i < outputs.length; i++) {
            outputs[i].getOutput().run(m.get(0,i));
        }
    }

    @Override
    public String toString(){
        String out = "";
        for (int i = 1; i < weights.length; i++) {
            out += "<";
            out += weights[i].getRowDimension();
            for (int j = 0;j < weights[i].getRowDimension();j++) {
                out += "{";
                for(int k = 0;k<weights[i].getColumnDimension();k++){
                    out += weights[i].get(j,k)+",";
                }

                out += "}";
            }
            out += ">";
        }

        return out;
    }
}
