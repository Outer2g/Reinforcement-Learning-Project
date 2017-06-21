package game;

import structs.FrameData;

import static utils.Utils.consoleLog;

/**
 * Created by outer2g on 30/04/17.
 */
public class FightingGameState {
    private Player player;
    private Player enemy;
    public FightingGameState(){
        player = new Player();
        enemy = new Player();
    }
    public FightingGameState(FrameData frameData, boolean player){
        this.player = new Player(frameData.getMyCharacter(player));
        this.enemy = new Player(frameData.getOpponentCharacter(player));
    }
    public Player getPlayer(){
        return player;
    }
    public Player getEnemy(){
        return enemy;
    }

    public void setPlayer(Player player){
        this.player = player;
    }
    public void setEnemy(Player enemy){
        this.enemy = enemy;
    }
}
