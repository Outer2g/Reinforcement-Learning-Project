package utils;

import game.FightingGameState;
import game.Player;

import java.util.ArrayList;

/**
 * Created by outer2g on 30/04/17.
 */
public class FeatureVector {

    public ArrayList<Integer> featureVector;
    public static final int N_FEATURES = 8;
    public FeatureVector(){}
    public FeatureVector(FightingGameState gameState){
        convertState(gameState);
    }
    private void extractFeatures(Player player){
        featureVector.add(player.position.x);
        featureVector.add(player.position.y);
        featureVector.add(player.hp);
        featureVector.add(player.playerState.ordinal());
    }
    public ArrayList<Integer> convertState(FightingGameState gameState){
        if (gameState != null) {


            featureVector = new ArrayList<Integer>();
            extractFeatures(gameState.getPlayer());
            extractFeatures(gameState.getEnemy());

            return featureVector;
        }
        return new ArrayList<Integer>();
    }
}
