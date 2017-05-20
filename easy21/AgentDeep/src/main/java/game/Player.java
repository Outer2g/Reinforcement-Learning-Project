package game;

import enumerate.Action;
import enumerate.State;
import structs.CharacterData;
import utils.Position;

/**
 * Created by outer2g on 30/04/17.
 */
public class Player {
    public Position position;
    public int hp,energy;
    public State playerState;
    public Action action;
    public Player(){
        position = new Position();
        hp = energy =  0;
        playerState = State.STAND;
    }
    public Player(CharacterData player){
        //since player.x and player.y is deprecated...
        int x = (player.getLeft() + player.getRight()) / 2;
        int y = (player.getTop() + player.getBottom()) / 2;
        this.position = new Position(x,y);

        this.hp = player.getHp();
        this.playerState = player.getState();
        this.energy = player.getEnergy();
        this.action = player.getAction();
    }
}
