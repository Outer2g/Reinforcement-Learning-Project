import commandcenter.CommandCenter;
import enumerate.Action;
import game.FightingGameState;
import game.Player;
import game.Transition;
import gameInterface.AIInterface;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import utils.Stats;
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
    private NNSARSA agent;
    private FightingGameState gameState,previousGameState;
    private final String path = "data/aiData/AgentDeep04-SARSA";
    private final String statsPath = "stats/stats";
    private int frameNumber;
    private Stats stats;

    //one time at the start of each game
    public int initialize(GameData gameData, boolean player) {
        stats = new Stats();
        inputKey = new Key();
        this.player = player;
        frameData = new FrameData();
        double gamma = 0.4;
        this.agent = new NNSARSA(gamma);
        cc = new CommandCenter();
        first = true;
        try {
            agent.loadValueFunction(path);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            consoleLog("Neural Networks not yet generated");
        }
        consoleLog("Max x: " + gameData.getStageXMax());
        consoleLog("Max y: " + gameData.getStageYMax());
        consoleLog("Mean x: " + gameData.getStageXMax()/2.0);
        consoleLog("Mean y: " + gameData.getStageYMax()/2.0);
        consoleLog("Gamma being used: " + gamma);
        consoleLog("Actions");
        for (int i = 0; i < Utils.ReducedActions.values().length; i++) {
            consoleLog(Utils.ReducedActions.values()[i].name());

        }
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
        frameNumber = frameData.getFrameNumber();
        consoleLog("Frame: " + frameNumber);
        shouldProcess = (!frameData.getEmptyFlag() && frameData.getRemainingTimeMilliseconds() > 0);
        if (shouldProcess) {
            this.frameData = frameData;
            consoleLog("Getting GameState...");
            this.gameState = new FightingGameState(frameData,player);
            cc.setFrameData(this.frameData,player);
        }
    }
    private void updateStats(int reward){
        if (reward < 0)++stats.hitsReceived;
        else if (reward > 0) ++stats.hitsDealt;
    }
    //executed each frame
    public void processing() {
        if (shouldProcess && !first){
            if (agent.getIterationsToTrain() == agent.batchSize){
                consoleLog("Now Training!!!!");
                agent.trainNNByExperienceReplay();
            }
            if (frameNumber == 0){agent.newEpisode();}
            if (cc.getSkillFlag()){
                inputKey = cc.getSkillKey();
            }
            else {
                consoleLog("calculating reward...");
                int reward = Utils.calculateReward(gameState, previousGameState, player);
                updateStats(reward);
                consoleLog("Reward: " + reward);
                consoleLog("Getting action...");
                Utils.ReducedActions action = Utils.ReducedActions.values()[agent.actEpsilonGreedy(gameState)];
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
            Utils.ReducedActions action = Utils.ReducedActions.values()[agent.actEpsilonGreedy(gameState)];
            consoleLog("Action to be taken: " + action.name());
            previousGameState = gameState;
            inputKey.empty();
            cc.commandCall(action.name());
            first = false;
        }
        if(frameData.getRemainingFramesNumber() == 16){
            System.out.println("Writing stats " + stats.hitsDealt);
            updateStatsAfterRound();
            stats.printStats(statsPath);
            stats.resetStats();
        }
    }

    private void updateStatsAfterRound(){
        CharacterData playerInfo = frameData.getMyCharacter(player);
        CharacterData enemyInfo = frameData.getOpponentCharacter(player);
        if (playerInfo.getHp() < enemyInfo.getHp()) ++stats.loses;
        else ++stats.wins;
        ++stats.gamesPlayed;
        stats.damageReceived = playerInfo.getHp();
        stats.damageDealt = enemyInfo.getHp();
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
