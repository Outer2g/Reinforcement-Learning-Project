package utils;

import game.Action;
import game.State;
import game.StateRewardPair;

/**
 * Created by outer2g on 18/04/17.
 */
public class Transition {
    public State cstate,nstate;
    public Action actionTaken;
    public int rewardAchieved;
    public Transition(){}
    public Transition(State currentState,Action action, int reward,State nextState){
        this.cstate = currentState;
        this.nstate = nextState;
        this. actionTaken = action;
        this.rewardAchieved = reward;
    }
    public Transition(State currentState,Action action, StateRewardPair nextState){
        this(currentState,action,nextState.reward,nextState.state);
    }
}
