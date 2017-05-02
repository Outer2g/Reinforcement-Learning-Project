package game;

import enumerate.Action;

/**
 * Created by outer2g on 30/04/17.
 */
public class Transition {
    public FightingGameState cstate,nstate;
    public Action actionTaken;
    public int rewardAchieved;
    public Transition(){}
    public Transition(FightingGameState currentState, Action action, int reward, FightingGameState nextState){
        this.cstate = currentState;
        this.nstate = nextState;
        this. actionTaken = action;
        this.rewardAchieved = reward;
    }
}
