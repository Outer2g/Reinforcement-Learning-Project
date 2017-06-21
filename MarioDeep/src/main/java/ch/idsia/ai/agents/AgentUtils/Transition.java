package ch.idsia.ai.agents.AgentUtils;


/**
 * Created by outer2g on 30/04/17.
 */
public class Transition {
    public byte[][] cstate,nstate; // states are the bitmaps
    public Utils.ReducedActions actionTaken;
    public float rewardAchieved;
    public Transition(){}
    public Transition(byte[][] currentState, Utils.ReducedActions action, float reward, byte[][] nextState){
        this.cstate = currentState;
        this.nstate = nextState;
        this.actionTaken = action;
        this.rewardAchieved = 2 * ((reward + 300) / 500) -1;
    }
}
