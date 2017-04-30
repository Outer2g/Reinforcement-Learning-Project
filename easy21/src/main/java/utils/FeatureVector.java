package utils;

import game.State;

import java.util.ArrayList;

/**
 * Created by outer2g on 16/04/17.
 */
public class FeatureVector {
    private int playerSum,dealerSum;
    public ArrayList<Integer> featureVector;
    public FeatureVector(){}
    public FeatureVector(State gameState){
        convertState(gameState);
    }
    public ArrayList<Integer> convertState(State gameState){
        playerSum = gameState.getPlayerSum();
        dealerSum = gameState.getDealersCard();
        featureVector = new ArrayList<Integer>();
        featureVector.add(playerSum);
        featureVector.add(dealerSum);
        return featureVector;
    }
}
