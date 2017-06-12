package ch.idsia.ai.agents;

import ch.idsia.ai.agents.AgentUtils.ActionMapper;
import ch.idsia.ai.agents.AgentUtils.MNNQLearning;
import ch.idsia.ai.agents.AgentUtils.Transition;
import ch.idsia.ai.agents.AgentUtils.Utils;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import static ch.idsia.ai.agents.AgentUtils.Utils.consoleLog;

/**
 * Created by outer2g on 23/05/17.
 */
public class MarioDeep implements Agent {
    protected boolean Action[] = new boolean[Environment.numberOfButtons];
    protected String Name = "MarioDeep Agent";
    public MNNQLearning agent;
    public static final int stateSize = 15 * 11;
    private boolean first;
    private byte[][] previousState;
    private int lastMarioX, lastMarioMode;

    public void reset() {
        Action = new boolean[Environment.numberOfButtons];
        agent.newEpisode();
        previousState = new byte[15][11];
        first = false;

    }

    private byte[][] reduceSight(byte[][] world){
        int truncateX = 11;
        int truncateY = 7;
        byte ret[][] = new byte[world.length - truncateY][world[0].length - truncateX];

        for (int i = truncateY; i < world.length; i++) {
            for (int j = truncateX ; j < world[0].length; j++) {
                int finalPos[] = {i - truncateY, j - truncateX};
                ret[finalPos[0]][finalPos[1]] = world[i][j];
            }
        }
        return ret.clone();
    }


    private float calculateReward(Environment observation){
        float totalReward = 0;
        int marioX = (int) observation.getMarioFloatPos()[0];
        int posOffset = 1;
        if (marioX > lastMarioX){
            totalReward += 4.5;
            if (Action[Mario.KEY_SPEED]) totalReward += 1.5;
        }
        else if (marioX < lastMarioX){
            totalReward -= 2.0;
            if (Action[Mario.KEY_SPEED]) totalReward -= 0.5;
        }
        else totalReward -= 2.0;
        return totalReward;

    }
    public boolean[] getAction(Environment observation) {

        byte world[][] = reduceSight(observation.getCompleteObservation());
        //Utils.printMatrix(world);
        if (first){
            lastMarioX = (int) observation.getMarioFloatPos()[0];
            previousState = world;
            Utils.ReducedActions actionToDo = Utils.ReducedActions.values()[agent.actEpsilonGreedy(world)];
            consoleLog("Action to be taken: " + actionToDo.name());
            Action = ActionMapper.performAction(actionToDo);
            first = false;
        }
        else{
            consoleLog("Calculating reward...");
            float reward = calculateReward(observation);
//            System.out.println("MarioX: " + lastMarioX + " Current: " + (int) observation.getMarioFloatPos()[0]);
//            System.out.println("Reward achieved: " + reward);
            consoleLog("Reward achieved: " + reward);
            consoleLog("Getting Action...");
            Utils.ReducedActions actionToDo = Utils.ReducedActions.values()[agent.actEpsilonGreedy(world)];

            Transition transition = new Transition(previousState,actionToDo,reward,world);
            agent.learn(transition);
            Action = ActionMapper.performAction(actionToDo);
            previousState = world;
            lastMarioX = (int) observation.getMarioFloatPos()[0];
        }

//        System.out.println("World pos in mario: " + world[marioX][marioY]);
        return Action;
    }

    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }
}
