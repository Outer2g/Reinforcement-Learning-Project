import Agents.Agent;
import game.Action;
import game.Game;
import game.StateRewardPair;
import utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by outer2g on 2/03/17.
 */
public class ReadResults {
    private static class Results{
        public double alpha,gamma,winRate,hitRate;
        Results(double alpha,double gamma,double winRate,double hitRate){
            this.alpha = alpha;
            this.gamma = gamma;
            this.winRate = winRate;
            this.hitRate = hitRate;
        }
    }
    public static void main(String [ ] args) throws IOException, ClassNotFoundException {
        System.out.println("Welcome to Easy21 Game!");
        System.out.println("Using " + args[0]);
        ArrayList<Results> results = new ArrayList<Results>();
        for(double alpha = 0.1; alpha <=1.0; alpha += 0.1){
            for (double gamma = 0.0;gamma <= 1.0; gamma += 0.1){
                Agent agent = Util.getAgent(args[0],alpha,gamma);
                Game game = new Game(agent);
                System.out.println("for alpha "+ alpha + " and gamma " + gamma);
                agent.loadValueFunction("valueFunctions/" + args[0] + "/run/valueFunction-"+ alpha + "-" + gamma+".ser");
                int episodes = 100000;
                for (int i =0; i< episodes;++i){
                    agent.newEpisode();
                    StateRewardPair firstState = game.startRound();
                    agent.playGame(firstState.state,game);
                }
                results.add(new Results(alpha,gamma,(((double) agent.stats.wins / agent.stats.totalGames)*100),
                        (((double) agent.stats.actions[Action.ACTION_HIT] / agent.stats.totalActions) * 100)));
                agent.stats.printStats();
                System.out.println();
            }
        }
        Collections.sort(results, new Comparator<Results>() {
            public int compare(Results results, Results t1) {
                return Double.compare(results.winRate,t1.winRate);
            }
        });
        for (Results res : results){
            System.out.println("alpha: " + res.alpha + " gamma: " + res.gamma + " winrate: " + res.winRate + " hitrate: " + res.hitRate);
        }
        System.out.println("done.");
    }
    private static void runEpisode(int nEpisode){

    }
}
