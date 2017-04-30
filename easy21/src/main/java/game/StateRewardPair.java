package game;

/**
 * Created by outer2g on 2/03/17.
 */
public class StateRewardPair {
    public State state;
    public int reward;
    StateRewardPair(State state,int reward){
        this.state = state;
        this.reward = reward;
    }
    public void printState(){
        state.printState();
        if(state.isTerminal()) System.out.println("Gained reward: " + reward);
    }
}
