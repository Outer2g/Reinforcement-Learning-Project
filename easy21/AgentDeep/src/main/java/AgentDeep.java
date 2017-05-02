import commandcenter.CommandCenter;
import enumerate.Action;
import game.FightingGameState;
import game.Player;
import game.Transition;
import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import utils.Utils;

import java.io.IOException;

import static utils.Utils.consoleLog;

/**
 * Created by outer2g on 30/04/17.
 */
public class AgentDeep implements AIInterface {
    /* Workflow:
        getInformation
        processing
        input
     */

    private FrameData frameData;
    private Key inputKey;
    private boolean shouldProcess,player,first;
    private CommandCenter cc;
    private NNQLearning agent;
    private FightingGameState gameState,previousGameState;
    private final String path = "data/aiData/Neural";
    //one time at the start of each game
    public int initialize(GameData gameData, boolean player) {
        inputKey = new Key();
        this.player = player;
        frameData = new FrameData();
        this.agent = new NNQLearning(0.5,0.5);
        cc = new CommandCenter();
        first = true;
        try {
            agent.loadValueFunction(path);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Neural Networks not yet generated");
        }
        consoleLog("Max x: " + gameData.getStageXMax());
        consoleLog("Max y: " + gameData.getStageYMax());
        consoleLog("Mean x: " + gameData.getStageXMax()/2.0);
        consoleLog("Mean y: " + gameData.getStageYMax()/2.0);
        return 0;
    }
    //gets info each frame
    /* from getting started:
    When you use frameData received from getInformation(),
     you must always check if the condition "!frameData.emptyFlag && frameData.getRemainingTime() > 0" holds;
     otherwise, NullPointerException will occur. You must also check the same condition when you
     use the CommandCenter class.
     */
    public void getInformation(FrameData frameData) {
        shouldProcess = (!frameData.getEmptyFlag() && frameData.getRemainingTimeMilliseconds() > 0);
        if (shouldProcess) {
            this.frameData = frameData;
            consoleLog("Getting GameState...");
            this.gameState = new FightingGameState(frameData,player);
            cc.setFrameData(this.frameData,player);
        }
    }

    //executed each frame
    public void processing() {
        if (shouldProcess && !first){
            if (cc.getSkillFlag()){
                inputKey = cc.getSkillKey();
            }
            else {
                consoleLog("calculating reward...");
                int reward = Utils.calculateReward(gameState, previousGameState, player);
                consoleLog("Reward: " + reward);
                consoleLog("Getting action...");
                Action action = Action.values()[agent.actEpsilonGreedy(gameState)];
                consoleLog("Action: " + action.name());
                Transition transition = new Transition(previousGameState, action, reward, gameState);
                agent.learn(transition);
                inputKey.empty();
                cc.commandCall(action.name());
                previousGameState = gameState;
            }
        }
        else if (first && shouldProcess){
            consoleLog("getting action...");
            Action action = Action.values()[agent.actEpsilonGreedy(gameState)];
            consoleLog("Action to be taken: " + action.name());
            previousGameState = gameState;
            inputKey.empty();
            cc.commandCall(action.name());
            first = false;
        }
    }

    //receives inputted key from player. Boolean values
    public Key input() {
        return inputKey;
    }

    //one time after the end of each game
    public void close() {
        System.out.println("Game Closing...");
        System.out.println("Saving neural networks...");
        agent.saveValueFunction(path);
        System.out.println("Saved!");
    }

    //which character to use
    public String getCharacter() {
        return CHARACTER_ZEN;
    }
}
