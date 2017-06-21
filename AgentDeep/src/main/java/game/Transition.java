package game;

import enumerate.Action;
import utils.Utils;

/**
 * Created by outer2g on 30/04/17.
 */
public class Transition {
    public FightingGameState cstate,nstate;
    public Utils.ReducedActions actionTaken;
    public double rewardAchieved;
    public Transition(){}
    public Transition(FightingGameState currentState, Utils.ReducedActions action, int reward, FightingGameState nextState){
        this.cstate = currentState;
        this.nstate = nextState;
        this.actionTaken = action;
        this.rewardAchieved = reward/120.0;
    }
}
