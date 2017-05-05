package Agents;

import game.Action;
import game.Game;
import game.State;
import game.StateRewardPair;
import utils.Stats;


import java.io.*;
import java.util.Random;

/**
 * Created by outer2g on 2/03/17.
 */
public abstract class Agent {
    public Stats stats;
    private final Random rand = new Random();
    public  Action takeAction(State state){return null;}
    public abstract void learn(State state, Game game);
    public abstract void playGame(State state,Game game);
    public abstract void newEpisode();
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
    public abstract void saveValueFunction(String path);
    public abstract void loadValueFunction(String path) throws ClassNotFoundException, IOException;

    protected boolean epsilonRandom(double epsilon){
        double randomValue = rand.nextDouble();
        //with probability 1-epsilon we take a greedy action, else take random
         return epsilon <= randomValue;
    }
    protected void initTo0(double[][][] matrix){
        for (int i = 0; i < 10; ++i){
            for (int j = 0; j< 21;++j){
                for (int z = 0; z < 2;++z) matrix[i][j][z] = 0;
            }
        }
    }
    protected void initTo0(int[][][] matrix){
        for (int i = 0; i < 10; ++i){
            for (int j = 0; j< 21;++j){
                for (int z = 0; z < 2;++z) matrix[i][j][z] = 0;
            }
        }
    }
    private void printMatrix(int[][][] matrix){
        for (int i = 0; i < 10; ++i){
            for (int j = 0; j< 21;++j){
                for (int z = 0; z < 2;++z) System.out.print(matrix[i][j][z]);
            }
        }
        System.out.println();
    }
    private void printMatrix(double[][][] matrix){
        for (int i = 0; i < 10; ++i){
            for (int j = 0; j< 21;++j){
                for (int z = 0; z < 2;++z) System.out.print(matrix[i][j][z]);
            }
        }
        System.out.println();
    }
}
