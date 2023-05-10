package NeuralNetworking.Math;

public class Function {
    public final int dim;
    public double[] param;

    public Function(int dim, double[] p) {
        this.dim = dim;
        param = p;
    }

    public Function(int dim) {
        this.dim = dim;
        param = new double[dim];
    }

    public double f(double[] v){
        double x = 0;
        for (int i = 0; i < dim; i++) {
            x += (i<v.length ? v[i]:0)*param[i];
        }

        return x;
    }

    public Function getDerivative(){
        double[] p = new double[dim-1];
        for (int i = 1; i < dim; i++) {
            p[i-1] = this.param[i]*i;
        }

        return new Function(dim-1, p);
    }

    public double[] getParam() {
        return param;
    }

    public void setParam(double[] param) {
        this.param = param;
    }


}
