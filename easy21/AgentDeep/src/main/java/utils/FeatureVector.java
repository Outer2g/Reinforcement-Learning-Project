package utils;

import enumerate.Action;
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
    public static final int N_FEATURES = 11;
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
        featureVector.add((player.energy - Utils.meanEnergy) / Utils.highestEnergy);
    }
    public ArrayList<Double> convertState(FightingGameState gameState){
        if (gameState != null) {


            featureVector = new ArrayList<Double>();
            extractFeatures(gameState.getPlayer());
            Player enemy = gameState.getEnemy();
            extractFeatures(enemy);
            //action from opponent
            featureVector.add((double) ((enemy.action.ordinal() - (Action.values().length/2)) / Action.values().length));

            return featureVector;
        }
        return new ArrayList<Double>();
    }
}
