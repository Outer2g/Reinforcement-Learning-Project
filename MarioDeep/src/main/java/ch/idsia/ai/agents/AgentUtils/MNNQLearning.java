package ch.idsia.ai.agents.AgentUtils;


import ch.idsia.ai.agents.MarioDeep;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static ch.idsia.ai.agents.AgentUtils.Utils.consoleLog;


/**
 * Created by outer2g on 30/04/17.
 */
public class MNNQLearning {
    public ArrayList<Transition> replayMemory;
    private final Random rand = new Random();
    private double gamma;
    private int iteration,nIterToSwap,numberOfBatches,iterationsToTrain;
    public int batchSize;
    private double epsilon0,explore,finalEpsilon,epsilon;

    private boolean whichModel;
    MultiLayerNetwork model,model2;
    public MNNQLearning(double gamma){
        this.gamma = gamma;
        this.numberOfBatches = 5;
        this.batchSize = 512;
        this.nIterToSwap = 3000;
        this.whichModel = true;
        this.epsilon0 = 1.0;
        this.finalEpsilon = 0.05;
        this.explore = 4000.0;
        this.iterationsToTrain = 0;
        this.epsilon = this.epsilon0;
        replayMemory = new ArrayList<Transition>();
        MultiLayerConfiguration mlnconf = new NeuralNetConfiguration.Builder()
                .seed(123456)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1)
                .learningRate(0.0012)
                .updater(Updater.ADAM).momentum(0.9)
                .weightInit(WeightInit.XAVIER)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(MarioDeep.stateSize)
                        .nOut(120)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(120)
                        .nOut(75)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(75)
                        .nOut(50)
                        .activation(Activation.RELU)
                        .build())
                .layer(3,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(50)
                        .nOut(Utils.ReducedActions.values().length)
                        .build())
                .build();
//        NeuralNetConfiguration.ListBuilder conf = new NeuralNetConfiguration.Builder()
//                .seed(123456)
//                .iterations(1).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
//                .learningRate(0.012)
//                //.updater(Updater.NESTEROVS).momentum(0.9)
//                .updater(Updater.ADAM)
//                //.updater(Updater.RMSPROP).rmsDecay(conf.getRmsDecay())
//                .weightInit(WeightInit.XAVIER).regularization(true).l2(1e-4).list()
//                .layer(0, new ConvolutionLayer.Builder(8, 8)
//                        .nIn(FeatureVector.N_FEATURES)
//                        .nOut(16)
//                        .stride(4, 4)
//                        .activation("relu").build());
//
//        conf.layer(1, new ConvolutionLayer.Builder(4, 4).nIn(16).nOut(32).stride(2, 2).activation("relu").build());
//
//        conf.layer(2, new DenseLayer.Builder().nIn(32).nOut(256).activation("relu").build());
//
//        conf.layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation("identity").nIn(256).nOut(Utils.ReducedActions.values().length)
//                .build());
//        MultiLayerConfiguration mlnconf = conf.pretrain(false).backprop(true).build();
        model = new MultiLayerNetwork(mlnconf);
        model2 = new MultiLayerNetwork(mlnconf);

        model.init();
    }
    private boolean epsilonRandom(double epsilon){
        double randomValue = rand.nextDouble();
        //with probability 1-epsilon we take a greedy action, else take random
        return epsilon >= randomValue;
    }
    public int actEpsilonGreedy(byte[][] state){

        //with probability 1-epsilon we take a greedy action, else take random
        boolean takeGreedy = epsilonRandom(epsilon);
        //scale down epsilon
        epsilon -= (epsilon0 - finalEpsilon) / explore;
        if (takeGreedy){
            //take greedy
            return selectGreedyAction(state);
        }
        else return rand.nextInt(Utils.ReducedActions.values().length);
    }
    private ArrayList<Transition> getRandomSample(ArrayList<Transition> array,int size){
        ArrayList<Transition> ret = new ArrayList<Transition>(array);
        Collections.shuffle(ret);
        return new ArrayList<Transition>(ret.subList(0,size));
    }
    private MultiLayerNetwork getCurrentNetwork(){
        if(whichModel) return model;
        else return model2;
    }
    private MultiLayerNetwork getFrozenNetwork(){
        if(whichModel) return model2;
        else return model;
    }
    public int getIterationsToTrain(){
        return iterationsToTrain;
    }
    public void swapNetworks(){System.out.println("NN- Swapping Networks");whichModel = !whichModel;}

    public void trainNNByExperienceReplay(){
        batchSize = 3 *(replayMemory.size() / 4);
        for (int i = 0; i < numberOfBatches; i++) {
            ArrayList<Transition> batch = getRandomSample(replayMemory,batchSize);
            trainNN(batch);
        }
    }
    public void newEpisode(){
        replayMemory = new ArrayList<Transition>();
        iterationsToTrain = 0;
        epsilon = epsilon0;
    }
    private void trainNN(ArrayList<Transition> batch) {
        System.out.println("training");
        double[][] states = new double[batchSize][MarioDeep.stateSize]; // actually possibilities of feature
        for (int i = 0; i < batchSize; ++i) {
            byte[][] state = batch.get(i).cstate;
            for (int j = 0; j < state.length; j++) {
                for (int k = 0; k < state[0].length; k++) {
                    states[i][j] = state[j][k];
                }
            }
        }
        INDArray stateHistory = Nd4j.create(states);
        INDArray currentQValues = getCurrentNetwork().output(stateHistory);
        INDArray frozenQValues = getFrozenNetwork().output(stateHistory);
        ArrayList<INDArray> CQVals = new ArrayList<INDArray>();
        ArrayList<INDArray> OQVals = new ArrayList<INDArray>();
        for (int i = 0; i < Utils.ReducedActions.values().length; i++) {
            CQVals.add(currentQValues.getColumn(i));
            OQVals.add(frozenQValues.getColumn(i));

        }

        double[][] QValues = new double[batchSize][Utils.ReducedActions.values().length];

        for (int i = 0; i < batchSize; ++i) {
            float reward = batch.get(i).rewardAchieved;
            ArrayList<Double> QActions = new ArrayList<Double>();
            for (int j = 0; j < Utils.ReducedActions.values().length; j++) {
                QActions.add(OQVals.get(j).getDouble(i));
            }
            double maxOQVal = Collections.max(QActions);
            for (int j = 0; j < Utils.ReducedActions.values().length; j++) {
                QValues[i][j] = reward + (gamma * maxOQVal) + CQVals.get(j).getDouble(i);
            }
        }
        INDArray QvaluesArray = Nd4j.create(QValues);
        consoleLog("SizeState " + stateHistory.length());
        consoleLog("QvalsArraySize " + QvaluesArray.length());
        //getCurrentNetwork().fit(stateHistory, QvaluesArray);
    }

    public void learn(Transition transition){
        replayMemory.add(transition);
    }

    private double[][] getStateAsMatrix(byte[][] state){
        double [][] stateRequest = new double[1][MarioDeep.stateSize];
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[0].length; j++) {
                stateRequest[0][i] = state[i][j];
            }
        }
        return stateRequest;
    }
    private int getMaxQAction(INDArray Q){
        double max = Q.getColumn(0).getDouble(0);
        int action = 0;
        consoleLog("NN - Q values: " + Q);
        for (int i = 1; i < Utils.ReducedActions.values().length; i++) {
            if (max > Q.getColumn(i).getDouble(0)){
                action = i;
                max = Q.getColumn(i).getDouble(0);
            }
        }
        consoleLog("NN - Action Decided: " + Utils.ReducedActions.values()[action]);
        return action;
    }
    private int selectGreedyAction(byte[][] state){
        double [][] stateRequest = getStateAsMatrix(state);
        INDArray Q = getCurrentNetwork().output(Nd4j.create(stateRequest));
        return getMaxQAction(Q);
    }

    public void saveValueFunction(String path) {
        File file1 = new File(path+"-NN1");
        File file2 = new File(path+"-NN2");
        try {
            ModelSerializer.writeModel(getCurrentNetwork(),file1,true);
            ModelSerializer.writeModel(getFrozenNetwork(),file2,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadValueFunction(String path) throws ClassNotFoundException, IOException {

        File file1 = new File(path+"-NN1");
        File file2 = new File(path+"-NN2");
        model = ModelSerializer.restoreMultiLayerNetwork(file1);
        model2 = ModelSerializer.restoreMultiLayerNetwork(file2);
    }
}
