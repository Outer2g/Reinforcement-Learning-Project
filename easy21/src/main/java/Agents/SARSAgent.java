package Agents;

import game.Action;
import game.Game;
import game.State;
import game.StateRewardPair;
import utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by outer2g on 2/03/17.
 */
public class SARSAgent extends Agent {
    private double[][][] Q; //value function, [i][j][a] i: dealers Card, j: playerSum, a = action
    private int[][][] EgilibilityTrace;
    private ArrayList<State> S; // All States seen in this episode
    private ArrayList<Action> A; // All Actions taken in this episode
    //private int[][][] Nsa; // each position is the number of times that particular pair of
    // state-action has been visited


    private final int No = 100;
    private double gamma;
    private double lambda;
    private final Random rand = new Random();
    public SARSAgent(double lambda,double gamma){
        Q = new double[11][22][2]; //possibilities: 10 dealers, 21 playersum, 2 actions.
        initTo0(Q);
        EgilibilityTrace = new int[11][22][2];
        initTo0(EgilibilityTrace);// init Nsa with 0s
        this.lambda = lambda;
        this.gamma = gamma;
        stats = new Stats();
    }
    public void newEpisode(){
        initTo0(EgilibilityTrace);
        S = new ArrayList<State>();
        A = new ArrayList<Action>();
    }
    public void giveInitState(StateRewardPair firstState){
        S.add(firstState.state);
        A.add(new Action(rand.nextInt(Action.NPOSSIBILITIES)));
    }
    private int timesStateWasVisited(State state){
        return EgilibilityTrace[state.getDealersCard()][state.getPlayerSum()][0] +
                EgilibilityTrace[state.getDealersCard()][state.getPlayerSum()][0];
    }
    private int actEpsilonGreedy(State state, double epsilon){
        double randomValue = 0 + (1 - 0) * rand.nextDouble();
        //with probability 1-epsilon we take a greedy action, else take random
        if (1-epsilon > randomValue){
            //take greedy
            int dealerCard = state.getDealersCard();
            int playerSum = state.getPlayerSum();
            if(Q[dealerCard][playerSum][0] > Q[dealerCard][playerSum][1]) return 0;
            else return 1;
        }
        else return rand.nextInt(Action.NPOSSIBILITIES);
    }
    private double computeTDError(State state,int action){
        //compute state reward
        int reward = 0;
        if (state.isTerminal()){
            if (state.getWinner() == Game.PLAYER) reward = 1;
            else reward = -1;
        }
        int pastDealerCard = S.get(S.size()-1).getDealersCard();
        int pastPlayerSum = S.get(S.size()-1).getPlayerSum();
        int pastAction = A.get(A.size()-1).getAction();
        int dealerCard = state.getDealersCard();
        int playerSum = state.getPlayerSum();
        return reward + gamma * Q[dealerCard][playerSum][action] - Q[pastDealerCard][pastPlayerSum][pastAction];
    }
    private void updateEgilibilityTrace(State state){
        int pastDealerCard = S.get(S.size()-1).getDealersCard();
        int pastPlayerSum = S.get(S.size()-1).getPlayerSum();
        int pastAction = A.get(A.size()-1).getAction();
        ++EgilibilityTrace[pastDealerCard][pastPlayerSum][pastAction];
    }
    private void updateTraces(double TDError){
        for (int i = 0; i< S.size(); ++i){
            int dealersCard = S.get(i).getDealersCard();
            int playerSum = S.get(i).getPlayerSum();
            int a = A.get(i).getAction();
            double alpha;
            if(EgilibilityTrace[dealersCard][playerSum][a] != 0)
                alpha = 1 / EgilibilityTrace[dealersCard][playerSum][a];
            else alpha = 1.0;
            Q[dealersCard][playerSum][a] +=
                    alpha * TDError * EgilibilityTrace[dealersCard][playerSum][a];
            EgilibilityTrace[dealersCard][playerSum][a] *= gamma * lambda;
        }
    }
    public void learn(State state,Game game) {
        //take action using epsilon-greedy policy
        double epsilon = No /(No + timesStateWasVisited(state));
        int action = actEpsilonGreedy(state,epsilon);
        S.add(state);
        A.add(new Action(action));
        while(!state.isTerminal()){
        //make an observation
        StateRewardPair gamestate = game.step(state,new Action(action));

        // take new action for this new state using the same policy
        epsilon = No / (No + timesStateWasVisited(gamestate.state));
        action = actEpsilonGreedy(gamestate.state,epsilon);
        double TDError = computeTDError(gamestate.state,action);
        updateEgilibilityTrace(gamestate.state);
        updateTraces(TDError);
        state = gamestate.state;
        S.add(state);
        A.add(new Action(action));
    }
    state.printState();
    }
    private int selectGreedyAction(State state){
        int dealersCard = state.getDealersCard();
        int playersCard = state.getPlayerSum();
        if (Q[dealersCard][playersCard][0] > Q[dealersCard][playersCard][1]) return 0;
        else return 1;
    }
    public void playGameRandom(State state,Game game){

        while(!state.isTerminal()){
            //make an observation
            StateRewardPair gamestate = game.step(state,new Action(rand.nextInt(2)));
            state = gamestate.state;
        }
        if (state.getWinner() == Game.PLAYER) ++stats.wins;
        else if(state.getWinner() == Game.DEALER) ++ stats.loses;
        stats.updateRatio();
    }
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
    public void saveValueFunction(String path){
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(Q);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println("There's been an error saving the value function");
        }
    }
    public void loadValueFunction(String path) throws ClassNotFoundException, IOException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
        Q = (double[][][]) in.readObject();
        in.close();
    }
}
