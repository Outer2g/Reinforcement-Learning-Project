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
 * Created by outer2g on 9/03/17.
 */
public class QLAgent extends Agent {

    private double[][][] Q; //value function, [i][j][a] i: dealers Card, j: playerSum, a = action
    private int[][][] visited;
    private ArrayList<State> S; // All States seen in this episode
    private final Random rand = new Random();
    private double alpha,gamma,epsilon0,explore,finalEpsilon,epsilon;
    public QLAgent(double alpha, double gamma){
        Q = new double[11][22][2]; //possibilities: 10 dealers, 21 playersum, 2 actions.
        visited = new int[11][22][2];
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon0 = 1.0;
        this.finalEpsilon = 0.05;
        this.explore = 125.0;
        this.epsilon = this.epsilon0;
        initTo0(Q);
        stats = new Stats();

        initTo0(visited);
    }
    private int timesWasVisited(State state){
        int dealers = state.getDealersCard();
        int player = state.getPlayerSum();
        return visited[dealers][player][0] + visited[dealers][player][1];
    }
    private int actEpsilonGreedy(State state){

        //with probability 1-epsilon we take a greedy action, else take random
        boolean takeGreedy = epsilonRandom(epsilon);
        //scale down epsilon
        epsilon -= (epsilon0 - finalEpsilon) / explore;
        if (takeGreedy){
            //take greedy
            return selectGreedyAction(state);
        }
        else return rand.nextInt(Action.NPOSSIBILITIES);
    }
    private void updateValueFunction(State state, StateRewardPair gameState, int action){
        int dealerS = state.getDealersCard();
        int playerS = state.getPlayerSum();
        int nextDealerS = gameState.state.getDealersCard();
        int nextPlayerS = gameState.state.getPlayerSum();
        int reward = gameState.reward;
        Q[dealerS][playerS][action] += alpha*(reward +
                (gamma * Math.max(Q[nextDealerS][nextPlayerS][0],Q[nextDealerS][nextPlayerS][1])) -
                    Q[dealerS][playerS][action]);
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
            updateValueFunction(state,gameState,action);
            state = gameState.state;
        }

        if (state.getWinner() == Game.PLAYER) ++stats.wins;
        else if(state.getWinner() == Game.DEALER) ++ stats.loses;
        stats.updateRatio();
    }

    private int selectGreedyAction(State state){
        int dealersCard = state.getDealersCard();
        int playersCard = state.getPlayerSum();
        if (Q[dealersCard][playersCard][0] > Q[dealersCard][playersCard][1]) return 0;
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
        S = new ArrayList<State>();
    }

    @Override
    public void saveValueFunction(String path) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(Q);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println("There's been an error saving the value function");
        }
    }

    @Override
    public void loadValueFunction(String path) throws ClassNotFoundException, IOException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
        Q = (double[][][]) in.readObject();
        in.close();
    }
}
