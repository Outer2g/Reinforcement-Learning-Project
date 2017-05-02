package utils;

import enumerate.State;
import game.FightingGameState;

/**
 * Created by outer2g on 30/04/17.
 */
public class Utils {
    public static final double stageMaxX = 960.0;
    public static final double stageMaxY = 640;
    public static final double stageMeanX= 480.0;
    public static final double stageMeanY = 320.0;
    public static final double lowestHP = -1400;
    public static final double meanHP = -700;
    public static final double meanState = State.values().length / 2;

    public static int calculateReward(FightingGameState gameState, FightingGameState gameState1, boolean player){
        int diffplayerHP = gameState.getPlayer().hp;
        diffplayerHP -= gameState1.getPlayer().hp;
        int diffenemyHP = gameState1.getEnemy().hp;
        diffenemyHP -= gameState.getEnemy().hp;
        consoleLog("Reward - Enemy: " + gameState.getEnemy().hp + " Enemy': " + gameState1.getEnemy().hp);
        return diffplayerHP + diffenemyHP;
    }
    public static void consoleLog(String message){
        System.out.println(message);
    }
}
