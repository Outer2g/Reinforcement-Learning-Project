import Agents.SARSAgent;
import game.Game;
import game.StateRewardPair;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by outer2g on 2/03/17.
 */
public class ReadResults {
    public static void main(String [ ] args) throws IOException, ClassNotFoundException {
        System.out.println("Welcome to Easy21 Game!");
        for(double alpha = 0.0; alpha <=1.0; alpha += 0.1){
            for (double gamma = 0.0;gamma <= 1.0; gamma += 0.1){
                SARSAgent agent = new SARSAgent(alpha,gamma);
                Game game = new Game(agent);
                System.out.println("for alpha "+ alpha + " and gamma " + gamma);
                agent.loadValueFunction("valueFunctions/Q-Learning/run/valueFunction-"+ alpha + "-" + gamma+".ser");
                int episodes = 20000;
                for (int i =0; i< episodes;++i){
                    agent.newEpisode();
                    StateRewardPair firstState = game.startRound();
                    agent.playGame(firstState.state,game);
                }

                agent.stats.printStats();
                System.out.println();
            }
        }
        System.out.println("done.");
    }
    private static void runEpisode(int nEpisode){

    }
}
