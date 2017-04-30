import Agents.LFAQLAgent;
import Agents.NNQLAgent;
import game.Game;
import game.StateRewardPair;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by outer2g on 25/04/17.
 */
public class LFAMain {
    public static void main(String [ ] args) throws IOException, ClassNotFoundException {
        System.out.println("Welcome to Easy21 Game!");
        System.out.println("Please Insert which kind of game you wanna play:");
        Scanner in = new Scanner(System.in);
        int option = in.nextInt();
//        int option = Integer.parseInt(args[0]);
        System.out.print("Pick lambda:");
        //double lambda = in.nextDouble();
        double lambda = Double.parseDouble(args[1]);
        System.out.print("Pick gamma:");
        //double gamma = in.nextDouble();
        double gamma = Double.parseDouble(args[2]);
        LFAQLAgent agent = new LFAQLAgent(lambda,gamma);
        Game game = new Game(agent);
        switch (option) {
            case 1:
                System.out.println("Training from 0");
                break;
            case 2:
                System.out.println("Training using previous value function");
                break;
            case 3:
                System.out.println("Playing the game using the value function");
                break;
            default:
                break;
        }
        if (option == 1 || option == 2) {
            if (option == 2)
                agent.loadValueFunction("valueFunctions/LFAQ-Learning/run/user-friendly/valueFunction-" + lambda + "-" + gamma);
            while (true) {
                int totalEpisodes = 1;
                System.out.println("How many episodes at once you wanna play");
                //int episodes = in.nextInt();
                int episodes = Integer.parseInt(args[3]);
                if (episodes == 1) break;
                for (int i = 0; i < episodes; ++i) {
                    System.out.println("Episode: " + totalEpisodes);
                    System.out.println();
                    agent.newEpisode();
                    StateRewardPair firstState = game.startRound();
                    agent.learn(firstState.state, game);
                    ++totalEpisodes;

                    //agent.saveValueFunction("valueFunctions/LFAQ-Learning/run/user-friendly/valueFunction-"+ lambda + "-" + gamma);
                }
                break;
            }
            agent.saveValueFunction("valueFunctions/LFAQ-Learning/run/user-friendly/valueFunction-" + lambda + "-" + gamma);
        }
        if(option == 3) {
            while (true) {
                agent.loadValueFunction("valueFunctions/LFAQ-Learning/run/user-friendly/valueFunction-" + lambda + "-" + gamma);
                int totalGames = 1;
                System.out.println("How many games at once you wanna play");
                int episodes = in.nextInt();
                for (int i = 0; i < episodes; ++i) {
                    System.out.println("Game: " + totalGames);
                    StateRewardPair firstState = game.startRound();
                    agent.playGame(firstState.state, game);
                    ++totalGames;
                    agent.stats.printStats();
                    System.out.println();
                }
                System.out.println("W: ");
                agent.printW();
            }
        }
    }
}
