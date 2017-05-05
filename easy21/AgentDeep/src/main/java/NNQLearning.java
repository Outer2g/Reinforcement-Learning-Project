import enumerate.Action;
import game.FightingGameState;
import game.Transition;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
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
    private final Random rand = new Random();
    private double alpha,gamma;
    private int iteration,nIterToSwap,numberOfBatches,iterationsToTrain;
    public int batchSize;
    private double epsilon0,explore,finalEpsilon,epsilon;

    private boolean whichModel;
    MultiLayerNetwork model,model2;
    public NNQLearning(double alpha, double gamma){
        this.alpha = alpha;
        this.gamma = gamma;
        this.numberOfBatches = 5;
        this.batchSize = 512; //normal game is around 1000 iterations => replaymemorysize = nIterations
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
                        .nIn(FeatureVector.N_FEATURES)
                        .nOut(25)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(25)
                        .nOut(50)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(2, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(50)
                        .nOut(50)
                        .activation(Activation.IDENTITY)
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
    private void swapNetworks(){consoleLog("NN- Swapping Networks");whichModel = !whichModel;}

    public void trainNNByExperienceReplay(){
        for (int i = 0; i < numberOfBatches; i++) {
            ArrayList<Transition> batch = getRandomSample(replayMemory,batchSize);
            trainNN(batch);
        }
    }
    public void newEpisode(){
        replayMemory = new ArrayList<Transition>();
        iterationsToTrain = 0;
    }
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
        for (int i = 0; i < Utils.ReducedActions.values().length; i++) {
            CQVals.add(currentQValues.getColumn(i));
            OQVals.add(frozenQValues.getColumn(i));

        }

        double[][] QValues = new double[batchSize][Utils.ReducedActions.values().length];

        for (int i = 0; i < batchSize; ++i) {
            int reward = batch.get(i).rewardAchieved;
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
        model.fit(stateHistory, QvaluesArray);
    }

    public void learn(Transition transition){
        replayMemory.add(transition);
        ++iteration;
        ++iterationsToTrain;
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
        for (int i = 1; i < Utils.ReducedActions.values().length; i++) {
            if (max > Q.getColumn(i).getDouble(0)){
                action = i;
                max = Q.getColumn(i).getDouble(0);
            }
        }
        consoleLog("NN - Action Decided: " + Utils.ReducedActions.values()[action]);
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
