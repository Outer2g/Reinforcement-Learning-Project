
import Agents.Agent;
import game.Game;
import game.StateRewardPair;
import utils.Util;


import java.io.IOException;
import java.util.Scanner;

/**
 * Created by outer2g on 2/03/17.
 */
public class Main {
    public static void main(String [ ] args) throws IOException, ClassNotFoundException {
        System.out.println("Welcome to Easy21 Game!");
        System.out.println("Using " + args[0]);
        System.out.println("Please Insert which kind of game you wanna play:");
        Scanner in = new Scanner(System.in);
//        int option = in.nextInt();
        int option = Integer.parseInt(args[1]);
        System.out.print("Pick alpha:");
//        double alpha = in.nextDouble();
        double alpha = Double.parseDouble(args[2]);
        System.out.print("Pick gamma:");
//        double gamma = in.nextDouble();
        double gamma = Double.parseDouble(args[3]);
        Agent agent = Util.getAgent(args[0],alpha,gamma);
        String path = "valueFunctions/" + args[0] + "/run/valueFunction-"+ alpha + "-" + gamma+".ser";
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
            case 4:
                System.out.println("Playing the game randomly");
            default:
                break;
        }
        if (option == 1 || option == 2){
            if (option == 2) agent.loadValueFunction(path);
            int episodes = Integer.parseInt(args[4]);
            while(true){
                int totalEpisodes = 1;
                System.out.println("How many episodes at once you wanna play");
                System.out.println("Executing " + episodes + " episodes...");
                for (int i = 0; i < episodes;++i) {
                    if (totalEpisodes == 5000) {
                        System.out.println("Episode: " + i);
                        System.out.println();
                        totalEpisodes = 0;
                    }
                    agent.newEpisode();
                    StateRewardPair firstState = game.startRound();
                    agent.learn(firstState.state,game);
                    ++totalEpisodes;
                }
                System.out.println("Done!");
                agent.stats.printStats();
                agent.stats.resetStats();
                break;
            }

            System.out.println("Saving value function in: " + path);
            agent.saveValueFunction(path);
        }
        if (option == 3){
            agent.loadValueFunction(path);
            while(true){
                int totalGames = 1;
                System.out.println("How many games at once you wanna play");
                int episodes = in.nextInt();
                for (int i = 0; i < episodes;++i) {
                    System.out.println("Game: " + totalGames);
                    StateRewardPair firstState = game.startRound();
                    agent.playGame(firstState.state,game);
                    ++totalGames;
                    agent.stats.printStats();
                    System.out.println();
                }
            }
        }
        else if (option == 4){
            while(true){
                int totalGames = 1;
                System.out.println("How many games at once you wanna play");
                int episodes = in.nextInt();
                for (int i = 0; i < episodes;++i) {
                    System.out.println("Game: " + totalGames);
                    StateRewardPair firstState = game.startRound();
                    agent.playGameRandom(firstState.state,game);
                    ++totalGames;
                    agent.stats.printStats();
                    System.out.println();
                }
            }
        }
}
    private static void runEpisode(int nEpisode){

    }
}
