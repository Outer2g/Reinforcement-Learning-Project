import enumerate.Action;
import game.FightingGameState;
import game.Transition;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import utils.FeatureVector;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static utils.Utils.consoleLog;

/**
 * Created by outer2g on 30/04/17.
 */
public class NNQLearning{
    private ArrayList<Transition> replayMemory;
    private int[][][] visited;
    private final int No = 100;
    private final Random rand = new Random();
    private double alpha,gamma;
    private int batchSize,iteration,nIterToSwap,replayMemorySize;

    private double epsilon0,explore,finalEpsilon,epsilon;

    private boolean whichModel;
    MultiLayerNetwork model,model2;
    public NNQLearning(double alpha, double gamma){
        visited = new int[11][22][2];
        this.alpha = alpha;
        this.gamma = gamma;
        this.replayMemorySize = 5000;
        this.batchSize = 64;
        this.nIterToSwap = 100000;
        this.whichModel = true;
        this.epsilon0 = 1.0;
        this.finalEpsilon = 0.05;
        this.explore = 1000.0;
        this.epsilon = this.epsilon0;
        replayMemory = new ArrayList<Transition>();
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123456)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1)
                .learningRate(0.012)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(FeatureVector.N_FEATURES)
                        .nOut(100)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(100)
                        .nOut(200)
                        .activation(Activation.IDENTITY)
                        .build()).layer(1, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(200)
                        .nOut(100)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(2,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(100)
                        .nOut(Action.values().length)
                        .build())
                .build();
        model = new MultiLayerNetwork(conf);
        model2 = new MultiLayerNetwork(conf);

        model.init();
    }
    private boolean epsilonRandom(double epsilon){
        double randomValue = rand.nextDouble();
        //with probability 1-epsilon we take a greedy action, else take random
        return epsilon <= randomValue;
    }
    protected int actEpsilonGreedy(FightingGameState state){

        //with probability 1-epsilon we take a greedy action, else take random
        boolean takeGreedy = epsilonRandom(epsilon);
        //scale down epsilon
        epsilon -= (epsilon0 - finalEpsilon) / explore;
        if (takeGreedy){
            //take greedy
            return selectGreedyAction(state);
        }
        else return rand.nextInt(Action.values().length);
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
    private void swapNetworks(){consoleLog("NN- Swapping Networks");whichModel = !whichModel;}

    private void trainNN(ArrayList<Transition> batch) {
        System.out.println("training");
        double[][] states = new double[batchSize][FeatureVector.N_FEATURES]; // actually possibilities of feature
        for (int i = 0; i < batchSize; ++i) {
            FeatureVector featureVector = new FeatureVector(batch.get(i).cstate);
            for (int j = 0; j < FeatureVector.N_FEATURES; j++) {
                states[i][j] = featureVector.featureVector.get(j);
            }
        }
        INDArray stateHistory = Nd4j.create(states);
        INDArray currentQValues = getCurrentNetwork().output(stateHistory);
        INDArray frozenQValues = getFrozenNetwork().output(stateHistory);
        ArrayList<INDArray> CQVals = new ArrayList<INDArray>();
        ArrayList<INDArray> OQVals = new ArrayList<INDArray>();
        for (int i = 0; i < Action.values().length; i++) {
            CQVals.add(currentQValues.getColumn(i));
            OQVals.add(frozenQValues.getColumn(i));

        }

        double[][] QValues = new double[batchSize][Action.values().length];

        for (int i = 0; i < batchSize; ++i) {
            int reward = batch.get(i).rewardAchieved;
            ArrayList<Double> QActions = new ArrayList<Double>();
            for (int j = 0; j < Action.values().length; j++) {
                QActions.add(OQVals.get(j).getDouble(i));
            }
            double maxOQVal = Collections.max(QActions);
            for (int j = 0; j < Action.values().length; j++) {
                QValues[i][j] = reward + (gamma * maxOQVal) + CQVals.get(j).getDouble(i);
            }
        }
        INDArray QvaluesArray = Nd4j.create(QValues);
        System.out.println("SizeState " + stateHistory.length());
        System.out.println("QvalsArraySize " + QvaluesArray.length());
        model.fit(stateHistory, QvaluesArray);
    }
//    private void trainNN(ArrayList<Transition> batch){
//        double [][] states = new double [batchSize][FeatureVector.N_FEATURES];
//        for (int i = 0 ; i < batchSize; ++i){
//            FeatureVector featureVector = new FeatureVector(batch.get(i).cstate);
//            for (int j = 0; j < FeatureVector.N_FEATURES; j++) {
//                states[i][j] = featureVector.featureVector.get(j);
//            }
//        }
//        INDArray stateHistory = Nd4j.create(states);
//
//        consoleLog("DEBUG - NN - states - " + stateHistory);
//        //get values from current and frozen network
//        INDArray currentQValues = getCurrentNetwork().output(stateHistory);
//        INDArray frozenQValues = getFrozenNetwork().output(stateHistory);
//        consoleLog("DEBUG - NN - currentQValues - " + currentQValues);
//        consoleLog("DEBUG - NN - frozenQValues - " + frozenQValues);
//        //pick them agruping by action
//        ArrayList<INDArray> CQVals = new ArrayList<INDArray>();
//        ArrayList<INDArray> OQVals = new ArrayList<INDArray>();
//        for (int i = 0; i < Action.values().length; i++) {
//            CQVals.add(currentQValues.getColumn(i));
//            OQVals.add(frozenQValues.getColumn(i));
//        }
//        consoleLog("DEBUG - NN - CQVals - " + CQVals);
//        consoleLog("DEBUG - NN - OQVals - " + OQVals);
//
//        double [][] QValues = new double[batchSize][Action.values().length];
//
//        for (int i = 0; i < batchSize; ++i){
//            int reward = batch.get(i).rewardAchieved;
//            ArrayList<Double> QActions = new ArrayList<Double>();
//            for (int j = 0; j < Action.values().length; j++) {
//                QActions.add(OQVals.get(j).getDouble(i));
//            }
//            double maxOQVal = Collections.max(QActions);
//            for (int j = 0; j <Action.values().length; j++) {
//                QValues[i][j] = reward + (gamma * maxOQVal) + CQVals.get(j).getDouble(i);
//            }
//        }
//        INDArray QvaluesArray = Nd4j.create(QValues);
//        consoleLog("DEBUG - NN - stateHistory - " + stateHistory);
//        consoleLog("DEBUG - NN - QValuesArray - " + QvaluesArray);
//        consoleLog("DEBUG - NN - sizeStateHistory - " + stateHistory.shapeInfoToString());
//        consoleLog("DEBUG - NN - QvaluesArray - " + QvaluesArray.shapeInfoToString());
//        model.fit(stateHistory,QvaluesArray);
//
//    }

    public void learn(Transition transition) {
            if(replayMemory.size() >= replayMemorySize) replayMemory.remove(0);
            replayMemory.add(transition);
            if (replayMemory.size() > batchSize) {
                ArrayList<Transition> batch = getRandomSample(replayMemory, batchSize);
                trainNN(batch);
            }
            ++iteration;
            consoleLog("NN - Iteration: " + iteration);
            if(iteration == nIterToSwap){
                swapNetworks();
                iteration = 0;
            }
    }

    private double[][] getStateAsMatrix(FightingGameState state){
        double [][] stateRequest = new double[1][FeatureVector.N_FEATURES];
        ArrayList<Double> featureVector = new FeatureVector(state).featureVector;
        for (int i = 0; i < FeatureVector.N_FEATURES; i++) {
            stateRequest[0][i] = featureVector.get(i);
        }
        return stateRequest;
    }
    private int getMaxQAction(INDArray Q){
        double max = Q.getColumn(0).getDouble(0);
        int action = 0;
        consoleLog("NN - Q values: " + Q);
        for (int i = 1; i < FeatureVector.N_FEATURES; i++) {
            if (max > Q.getColumn(i).getDouble(0)){
                action = i;
                max = Q.getColumn(i).getDouble(0);
            }
        }
        consoleLog("NN - Action Decided: " + Action.values()[action]);
        return action;
    }
    private int selectGreedyAction(FightingGameState state){
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
