package NeuralNetworking;

import NeuralNetworking.Change.BrainType;

import java.io.Serializable;
import java.util.LinkedList;

public abstract class BrainObject implements Comparable<BrainObject>, Serializable {
    public BrainType brain;
    private int tick = 1;
    public boolean isBest = false;
    public abstract double getFitness();
    public abstract boolean isDead();
    public abstract void update();
    public abstract void reset();
    public abstract String fitnessReport();

    public BrainType getBrain() {
        return brain;
    }

    public void tick(){
        if (!isDead()){
            tick++;
            this.brain.update();
            this.update();
        }
    }

    public void setBrain(BrainType brain) {
        this.brain = brain;
    }

    @Override
    public int compareTo(BrainObject that){
        return (int)Math.round(this.getFitness() - that.getFitness());
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public static boolean isDead(LinkedList<BrainObject> list){
        for(BrainObject b:list)
            if (!b.isDead())
                return false;
        return true;
    }

    public static void sort(LinkedList<BrainObject> objects){
        quickSort(objects,0 , objects.size()-1);
    }

    static int partition(LinkedList<BrainObject> objects, int low, int high)   {
        BrainObject pivot = objects.get(high);
        // smaller element index
        int i = (low - 1);
        for (int j = low; j <= high - 1; j++) {
            // check if current element is less than or equal to pivot
            if (objects.get(j).getFitness() <= pivot.getFitness()) {
                i++;
                // swap the elements
                BrainObject temp = objects.get(i);
                objects.set(i, objects.get(j));
                objects.set(j, temp);
            }
        }
        // swap numArray[i+1] and numArray[high] (or pivot)
        BrainObject temp = objects.get(i+1);
        objects.set(i+1, objects.get(high));
        objects.set(high, temp);
        return i + 1;
    }


    //sort the array using quickSort
    static void quickSort(LinkedList<BrainObject> objects, int low, int high)
    {
        //auxillary stack
        int[] intStack = new int[high - low + 1];

        // top of stack initialized to -1
        int top = -1;

        // push initial values of low and high to stack
        intStack[++top] = low;
        intStack[++top] = high;

        // Keep popping from stack while is not empty
        while (top >= 0) {
            // Pop h and l
            high = intStack[top--];
            low = intStack[top--];

            // Set pivot element at its correct position
            // in sorted array
            int pivot = partition(objects, low, high);

            // If there are elements on left side of pivot,
            // then push left side to stack
            if (pivot - 1 > low) {
                intStack[++top] = low;
                intStack[++top] = pivot - 1;
            }

            // If there are elements on right side of pivot,
            // then push right side to stack
            if (pivot + 1 < high) {
                intStack[++top] = pivot + 1;
                intStack[++top] = high;
            }
        }
    }
}
