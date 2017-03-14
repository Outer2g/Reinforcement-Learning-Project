import Agents.SARSAgent;
import game.Game;
import game.StateRewardPair;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by outer2g on 2/03/17.
 */
public class Main3 {
    public static void main(String [ ] args) throws IOException, ClassNotFoundException {
        System.out.println("Welcome to Easy21 Game!");
            for(double alpha = 0.0; alpha <=1.0; alpha += 0.1){
                for (double gamma = 0.0;gamma <= 1.0; gamma += 0.1){
                    System.out.println("starting 5 milions episodes for alpha " + alpha + " and gamma "+ gamma);
                    SARSAgent agent = new SARSAgent(alpha,gamma);
                    Game game = new Game(agent);
                    int episodes = 5000000;
                    for (int i =0; i< episodes;++i){
                        agent.newEpisode();
                        StateRewardPair firstState = game.startRound();
                        agent.learn(firstState.state,game);
                    }
                    agent.saveValueFunction("valueFunctions/Q-Learning/run/valueFunction-"+ alpha + "-" + gamma+".ser");
                }
            }
            System.out.println("done.");
        }
    private static void runEpisode(int nEpisode){

    }
}
