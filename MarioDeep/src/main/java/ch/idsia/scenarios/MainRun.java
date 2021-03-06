package ch.idsia.scenarios;

import ch.idsia.ai.agents.AgentUtils.MNNQLearning;
import ch.idsia.ai.agents.AgentUtils.MNNSARSA;
import ch.idsia.ai.agents.MarioDeep;
import ch.idsia.ai.agents.ai.*;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
//import ch.idsia.ai.agents.icegic.robin.AStarAgent;
//import ch.idsia.ai.agents.icegic.peterlawford.SlowAgent;
import ch.idsia.ai.agents.human.CheaterKeyboardAgent;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.Evaluator;
import ch.idsia.utils.StatisticalSummary;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import competition.cig.sergeykarakovskiy.SergeyKarakovskiy_JumpingAgent;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstName_at_idsia_dot_ch
 * Date: May 7, 2009
 * Time: 4:35:08 PM
 * Package: ch.idsia
 */

public class MainRun {
    final static int numberOfTrials = 20;
    final static boolean scoring = true;
    private static int killsSum = 0;
    private static int marioStatusSum = 0;
    private static int timeLeftSum = 0;
    private static int marioModeSum = 0;
    private static MarioDeep marioDeep;

    public static void main(String[] args) {
        CmdLineOptions cmdLineOptions = new CmdLineOptions(args);
        EvaluationOptions evaluationOptions = cmdLineOptions;  // if none options mentioned, all defalults are used.
        createAgentsPool();

        if (scoring)
            scoreAllAgents(cmdLineOptions);
        else {
            Evaluator evaluator = new Evaluator(evaluationOptions);
            List<EvaluationInfo> evaluationSummary = evaluator.evaluate();
//        LOGGER.save("log.txt");
        }

        if (cmdLineOptions.isExitProgramWhenFinished())
            System.exit(0);
    }

    private static boolean calledBefore = false;

    public static void createAgentsPool() {
        if (!calledBefore) {
            // Create an Agent here or mention the set of agents you want to be available for the framework.
            // All created agents by now are used here.
            // They can be accessed by just setting the commandline property -ag to the name of desired agent.
            calledBefore = true;
            //addAgentToThePool
//            AgentsPool.addAgent(new ForwardAgent());
//            AgentsPool.addAgent(new ForwardJumpingAgent());
//            AgentsPool.addAgent(new RandomAgent());
//            AgentsPool.addAgent(new CheaterKeyboardAgent());
//            AgentsPool.addAgent(new SimpleMLPAgent());
//            AgentsPool.addAgent(new ScaredAgent());
//            AgentsPool.addAgent(new Perez());
//            AgentsPool.addAgent(new AdaptiveAgent());
//            AgentsPool.addAgent(new AIwesome());
//            AgentsPool.addAgent(new TutchekAgent());
//            AgentsPool.addAgent(new SlowAgent());
//            AgentsPool.addAgent(new AStarAgent());
//            AgentsPool.addAgent(new RjAgent());
//            AgentsPool.addAgent(new SergeyKarakovskiy_JumpingAgent());
            marioDeep = new MarioDeep();
            AgentsPool.addAgent(marioDeep);
        }
    }

    public static void scoreAllAgents(CmdLineOptions cmdLineOptions) {
        for (Agent agent : AgentsPool.getAgentsCollection())
            score(agent, 3143, cmdLineOptions);
    }


    public static void score(Agent agent, int startingSeed, CmdLineOptions cmdLineOptions) {
        TimingAgent controller = new TimingAgent(agent);
//        RegisterableAgent.registerAgent (controller);
//        EvaluationOptions options = new CmdLineOptions(new String[0]);
        EvaluationOptions options = cmdLineOptions;

        options.setNumberOfTrials(1);
//        options.setVisualization(false);
//        options.setMaxFPS(true);
        System.out.println("\nScoring controller " + agent.getName() + " with starting seed " + startingSeed);

        double competitionScore = 0;
        killsSum = 0;
        marioStatusSum = 0;
        timeLeftSum = 0;
        marioModeSum = 0;

        competitionScore += testConfig(controller, options, startingSeed, 0, false,0.4);
        competitionScore += testConfig(controller, options, startingSeed, 0, false,0.3);
        competitionScore += testConfig(controller, options, startingSeed, 0, false,0.2);
        competitionScore += testConfig(controller, options, startingSeed, 0, false,0.1);
//        competitionScore += testConfig(controller, options, startingSeed, 3, false);
//        competitionScore += testConfig(controller, options, startingSeed, 5, false);
//        competitionScore += testConfig(controller, options, startingSeed, 10, false);
        System.out.println("Competition score: " + competitionScore);
        System.out.println("Total kills Sum = " + killsSum);
        System.out.println("marioStatus Sum  = " + marioStatusSum);
        System.out.println("timeLeft Sum = " + timeLeftSum);
        System.out.println("marioMode Sum = " + marioModeSum);
        System.out.println("TOTAL SUM for " + agent.getName() + " = " + (competitionScore + killsSum + marioStatusSum + marioModeSum + timeLeftSum));
    }

    public static double testConfig(TimingAgent controller, EvaluationOptions options, int seed, int levelDifficulty, boolean paused,double gamma) {
        options.setLevelDifficulty(levelDifficulty);
        options.setPauseWorld(paused);
        StatisticalSummary ss = test(controller, options, seed,gamma);
        double averageTimeTaken = controller.averageTimeTaken();
        System.out.printf("Difficulty %d score %.4f (avg time %.4f)\n",
                levelDifficulty, ss.mean(), averageTimeTaken);
        if (averageTimeTaken > 40) {
            System.out.println("Maximum allowed average time is 40 ms per time step.\n" +
                    "Controller disqualified");
            System.exit(0);
        }
        return ss.mean();
    }

    public static StatisticalSummary test(Agent controller, EvaluationOptions options, int seed,double gamma) {
        StatisticalSummary ss = new StatisticalSummary();
        int kills = 0;
        int timeLeft = 0;
        int marioMode = 0;
        int marioStatus = 0;
        String pathToNN = "marioValueFunctions/NNSARSA"+gamma;
        System.out.println("using: "+pathToNN);
        String pathToStats = "stats/SARSA" + gamma;
        try {
            marioDeep.agent = new MNNQLearning(gamma);

            marioDeep.agent.loadValueFunction(pathToNN);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        options.setNumberOfTrials(numberOfTrials);
        options.resetCurrentTrial();
        options.setLevelDifficulty(0);
        options.setMaxFPS(true);
        for (int i = 0; i < 40; i++) {
            System.out.println("Starting trial " + i + " for controller " + controller.getName() + ' '+gamma);
            options.setLevelRandSeed(seed);
            options.setLevelLength(200);
            options.setLevelType(0);
            options.setTimeLimit(180);
            controller.reset();
            options.setAgent(controller);
            Evaluator evaluator = new Evaluator(options);
            System.out.println("evaluating...");
            EvaluationInfo result = evaluator.evaluate().get(0);
            int lastTransitionId = marioDeep.agent.replayMemory.size() - 1;
                if (result.marioStatus == 0) marioDeep.agent.replayMemory.get(lastTransitionId).rewardAchieved -= 300.0;
                else marioDeep.agent.replayMemory.get(lastTransitionId).rewardAchieved += 200.0;
            System.out.println("Episode done, now training... Size: " + marioDeep.agent.replayMemory.size());
            marioDeep.agent.trainNNByExperienceReplay();
            kills += result.computeKillsTotal();
            timeLeft += result.timeLeft;
            marioMode += result.marioMode;
            marioStatus += result.marioStatus;
            System.out.println("\ntrial # " + i);
            System.out.println("result.timeLeft = " + result.timeLeft);
            System.out.println("result.marioMode = " + result.marioMode);
            System.out.println("result.marioStatus = " + result.marioStatus);
            System.out.println("result.computeKillsTotal() = " + result.computeKillsTotal());
            System.out.println("saving NN");
            printStats(result,pathToStats);
            //if (i % 2 == 0) marioDeep.agent.swapNetworks();
            marioDeep.agent.saveValueFunction(pathToNN);
            marioDeep.agent.newEpisode();
            ss.add(result.computeDistancePassed());
        }

        System.out.println("\n===================\nStatistics over 10 runs for " + controller.getName());
        System.out.println("Total kills = " + kills);
        System.out.println("marioStatus = " + marioStatus);
        System.out.println("timeLeft = " + timeLeft);
        System.out.println("marioMode = " + marioMode);
        System.out.println("===================\n");

        killsSum += kills;
        marioStatusSum += marioStatus;
        timeLeftSum += timeLeft;
        marioModeSum += marioMode;

        return ss;
    }
    private static void printStats(EvaluationInfo result, String path){
        PrintWriter output = null;
        try {
            output = new PrintWriter(new FileWriter(path,true));
            int wins = 0;
            int loses = 0;
            int gamesPlayed = 1;
            if (result.marioStatus == 0 || result.timeLeft == 0) loses = 1;
            else wins =1;
            output.println("wins: " + wins );
            output.println("loses: " + loses );
            output.println("gamesPlayed: " + gamesPlayed);
            output.println("TimeLeft: " + result.timeLeft);
            output.println("EnemiesKilled: " + result.killsTotal);
            output.println("score: "    + (killsSum + marioStatusSum + marioModeSum + timeLeftSum) );
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
