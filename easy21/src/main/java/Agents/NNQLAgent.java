package Agents;

import game.Action;
import game.Game;
import game.State;
import game.StateRewardPair;
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
import utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by outer2g on 15/04/17.
 */
public class NNQLAgent extends Agent{
    private ArrayList<Transition> replayMemory;
    private int[][][] visited;
    private ArrayList<State> S; // All States seen in this episode
    private final int No = 100;
    private final Random rand = new Random();
    private double alpha,gamma,replayMemorySize;
    private int batchSize,iteration,nIterToSwap;
    private boolean whichModel;
    MultiLayerNetwork model,model2;
    public NNQLAgent(double alpha, double gamma){
        visited = new int[11][22][2];
        this.alpha = alpha;
        this.gamma = gamma;
        this.replayMemorySize = 5000;
        this.batchSize = 2000;
        this.nIterToSwap = 10000;
        this.whichModel = true;
        stats = new Stats();
        replayMemory = new ArrayList<Transition>();
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123456)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(1)
                .learningRate(0.006)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .regularization(true).l2(1e-4)
                .list()
                .layer(0, new DenseLayer.Builder()
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(2)
                        .nOut(20)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1,new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(20)
                        .nOut(2)
                        .build())
                .build();
        model = new MultiLayerNetwork(conf);
        model2 = new MultiLayerNetwork(conf);

        model.init();
        initTo0(visited);
    }
    private int timesWasVisited(State state){
        int dealers = state.getDealersCard();
        int player = state.getPlayerSum();
        return visited[dealers][player][0] + visited[dealers][player][1];
    }
    private int actEpsilonGreedy(State state){
        double epsilon = No /(No + timesWasVisited(state));
        //with probability 1-epsilon we take a greedy action, else take random
        if (epsilonRandom(epsilon)){
            //take greedy
            double [][] stateRequest = new double[1][2];
            stateRequest[0][0] = state.getPlayerSum();
            stateRequest[0][1] = state.getDealersCard();
            INDArray Q = getCurrentNetwork().output(Nd4j.create(stateRequest));
            if(Q.getColumn(0).getDouble(0) > Q.getColumn(1).getDouble(0)) return 0;
            else return 1;
        }
        else return rand.nextInt(Action.NPOSSIBILITIES);
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
    private void swapNetworks(){whichModel = !whichModel;}

    private void trainNN(ArrayList<Transition> batch,State state, StateRewardPair gameState, int action){
        double [][] states = new double [batchSize][2];
        for (int i = 0 ; i < batchSize; ++i){
            states[i][0] = batch.get(i).cstate.getPlayerSum();
            states[i][1] = batch.get(i).cstate.getDealersCard();
        }
        INDArray stateHistory = Nd4j.create(states);
        INDArray currentQValues = getCurrentNetwork().output(stateHistory);
        INDArray frozenQValues = getFrozenNetwork().output(stateHistory);
        INDArray CQVal0 = currentQValues.getColumn(0);
        INDArray CQVal1 = currentQValues.getColumn(1);
        INDArray OQVal0 = frozenQValues.getColumn(0);
        INDArray OQVal1 = frozenQValues.getColumn(1);

        double [][] QValues = new double[batchSize][2];

        for (int i = 0; i < batchSize; ++i){
            int reward = batch.get(i).rewardAchieved;
            double maxOQVal = Math.max(OQVal0.getDouble(i),OQVal1.getDouble(i));
            QValues[i][0] = reward + (gamma * maxOQVal) + CQVal0.getDouble(i);
            QValues[i][1] = reward + (gamma * maxOQVal) + CQVal1.getDouble(i);
        }
        INDArray QvaluesArray = Nd4j.create(QValues);
        model.fit(stateHistory,QvaluesArray);

    }
    private void updateVisited(State state,int action){
        int playerSum = state.getPlayerSum();
        int dealersCard = state.getDealersCard();
        ++visited[dealersCard][playerSum][action];
    }
    @Override
    public void learn(State state, Game game) {
        while (! state.isTerminal()){
            int action = actEpsilonGreedy(state);
            stats.updateActions(action);
            updateVisited(state,action);
            StateRewardPair gameState = game.step(state,new Action(action));
            if(replayMemory.size() >= replayMemorySize) replayMemory.remove(0);
            replayMemory.add(new Transition(state,new Action(action),gameState));
            if (replayMemory.size() > batchSize) {
                ArrayList<Transition> batch = getRandomSample(replayMemory, batchSize);
                trainNN(batch,state,gameState,action);
            }
            ++iteration;
            if(iteration % nIterToSwap == 0) swapNetworks();
            state = gameState.state;
        }
        if (state.getWinner() == Game.PLAYER) ++stats.wins;
        else if(state.getWinner() == Game.DEALER) ++ stats.loses;
        stats.updateRatio();
    }

    private int selectGreedyAction(State state){
        double [][] stateRequest = new double[1][2];
        stateRequest[0][0] = state.getPlayerSum();
        stateRequest[0][1] = state.getDealersCard();
        INDArray Q = getCurrentNetwork().output(Nd4j.create(stateRequest));
        if (Q.getColumn(0).getDouble(0) > Q.getColumn(1).getDouble(0)) return 0;
        else return 1;
    }
    @Override
    public void playGame(State state, Game game) {
        while(!state.isTerminal()){
            int action = selectGreedyAction(state);
            //make an observation
            StateRewardPair gamestate = game.step(state,new Action(action));
            state = gamestate.state;
        }
        if (state.getWinner() == Game.PLAYER) ++stats.wins;
        else if(state.getWinner() == Game.DEALER) ++ stats.loses;
        stats.updateRatio();
    }

    @Override
    public void newEpisode() {
        S = new ArrayList<State>();
    }

    @Override
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

    @Override
    public void loadValueFunction(String path) throws ClassNotFoundException, IOException {

        File file1 = new File(path+"-NN1");
        File file2 = new File(path+"-NN2");
        model = ModelSerializer.restoreMultiLayerNetwork(file1);
        model2 = ModelSerializer.restoreMultiLayerNetwork(file2);
    }
}




