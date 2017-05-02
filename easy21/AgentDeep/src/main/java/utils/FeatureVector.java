package utils;

import enumerate.State;
import game.FightingGameState;
import game.Player;

import javax.rmi.CORBA.Util;
import java.util.ArrayList;

/**
 * Created by outer2g on 30/04/17.
 */
public class FeatureVector {

    public ArrayList<Double> featureVector;
    public static final int N_FEATURES = 8;
    public FeatureVector(){}
    public FeatureVector(FightingGameState gameState){
        convertState(gameState);
    }
    private void extractFeatures(Player player){
        // normalized values
        featureVector.add((player.position.x - Utils.stageMeanX) / Utils.stageMaxX);
        featureVector.add((player.position.y - Utils.stageMeanY) / Utils.stageMaxY);
        featureVector.add((player.hp - Utils.meanHP) / Utils.lowestHP);
        featureVector.add((player.playerState.ordinal() - Utils.meanState) / State.values().length);
    }
    public ArrayList<Double> convertState(FightingGameState gameState){
        if (gameState != null) {


            featureVector = new ArrayList<Double>();
            extractFeatures(gameState.getPlayer());
            extractFeatures(gameState.getEnemy());

            return featureVector;
        }
        return new ArrayList<Double>();
    }
}
