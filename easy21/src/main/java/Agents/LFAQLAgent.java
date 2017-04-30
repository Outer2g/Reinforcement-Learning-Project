package Agents;

import game.Action;
import game.Game;
import game.State;
import game.StateRewardPair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by outer2g on 25/04/17.
 */
public class LFAQLAgent extends Agent  {

    private ArrayList<Transition> replayMemory;
    private DoubleMatrix w;
    private int[][][] visited;
    private final int No = 100;
    private final Random rand = new Random();
    private double alpha,gamma,replayMemorySize;
    private boolean whichModel;
    MultiLayerNetwork model,model2;
    public LFAQLAgent(double alpha, double gamma){
        visited = new int[11][22][2];
        w = DoubleMatrix.ones(2,2);
        this.alpha = alpha;
        this.gamma = gamma;
        this.replayMemorySize = 500;
        this.whichModel = true;
        stats = new Stats();
        replayMemory = new ArrayList<Transition>();

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
            return selectGreedyAction(state);
        }
        else return rand.nextInt(Action.NPOSSIBILITIES);
    }
    private DoubleMatrix transformToDoubleMatrix(FeatureVector v){
        DoubleMatrix featureVec = DoubleMatrix.zeros(2,2);
        double val = v.featureVector.get(0);
        double val1 = v.featureVector.get(1);
        featureVec.put(0,0,v.featureVector.get(0));
        featureVec.put(0,1,v.featureVector.get(1));
        featureVec.put(1,0,v.featureVector.get(0));
        featureVec.put(1,1,v.featureVector.get(1));

        return featureVec;
    }
    private void applyGradient(ArrayList<Transition> D){
        DoubleMatrix accumulated = DoubleMatrix.zeros(2,2);
        DoubleMatrix accumulated1 = DoubleMatrix.zeros(2,2);
        for (Transition transition : D) {
            State state = transition.cstate;
            State state1 = transition.nstate;
            double reward = transition.rewardAchieved;
            DoubleMatrix featureVec = transformToDoubleMatrix(new FeatureVector(state));
            DoubleMatrix featureVec1 = transformToDoubleMatrix(new FeatureVector(state1));
            DoubleMatrix featureVecAux = featureVec.dup();
            featureVecAux = featureVec.mul(featureVec);
            featureVecAux = featureVecAux.sub(gamma * featureVec1.get(actEpsilonGreedy(state1)));

            accumulated = accumulated.add(featureVecAux);

            accumulated1 = accumulated1.add(featureVec.mul(reward));
        }
        //Inverse matrix O(N³)
        DoubleMatrix inverse = Solve.pinv(accumulated);

        w = inverse.mul(accumulated1);
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
            updateVisited(state,action);
            StateRewardPair gameState = game.step(state,new Action(action));
            replayMemory.add(new Transition(state,new Action(action),gameState));
            if (replayMemory.size() > replayMemorySize) {
                applyGradient(replayMemory);
                replayMemory = new ArrayList<Transition>();
            }
            state = gameState.state;
        }
    }

    private int selectGreedyAction(State state){
        DoubleMatrix Q = transformToDoubleMatrix(new FeatureVector(state));
        Q = Q.mul(w);
        if (Q.get(0) > Q.get(1)) return 0;
        else return 1;
    }
    @Override
    public void playGame(State state, Game game) {
        while(!state.isTerminal()){
            int action = selectGreedyAction(state);
            stats.updateActions(action);
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

    }

    @Override
    public void saveValueFunction(String path) {
        try {
            w.save(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void printW(){
        w.print();
    }

    @Override
    public void loadValueFunction(String path) throws ClassNotFoundException, IOException {
        w.load(path);
    }
}
