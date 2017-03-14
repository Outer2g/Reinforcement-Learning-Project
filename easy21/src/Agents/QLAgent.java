package Agents;

import game.Action;
import game.Game;
import game.State;
import game.StateRewardPair;

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
    private final int No = 100;
    private final Random rand = new Random();
    private double alpha,gamma;
    public QLAgent(double alpha, double gamma){
        this.alpha = alpha;
        this.gamma = gamma;
        initTo0(Q);

        initTo0(visited);
    }
    private int timesWasVisited(State state){
        int dealers = state.getDealersCard();
        int player = state.getPlayerSum();
        return visited[dealers][player][0] + visited[dealers][player][1];
    }
    private int actEpsilonGreedy(State state){
        double epsilon = No /(No + timesWasVisited(state));
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
    @Override
    public void learn(State state, Game game) {
        while (! state.isTerminal()){
            int action = actEpsilonGreedy(state);
            StateRewardPair gameState = game.step(state,new Action(action));
            updateValueFunction(state,gameState,action);
            state = gameState.state;
        }
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
