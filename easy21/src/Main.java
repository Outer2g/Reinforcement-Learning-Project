import Agents.Agent;
import Agents.Human;
import game.Game;
import game.State;
import game.StateRewardPair;

import java.util.Scanner;

/**
 * Created by outer2g on 2/03/17.
 */
public class Main {
    public static void main(String [ ] args){
        System.out.println("Welcome to Easy21 Game!");
        System.out.println("Please Insert which kind of game you wanna play:");
        Scanner in = new Scanner(System.in);
        int option = 1;
        Agent agent = new Human();
        Game game = new Game(agent);
        switch (option) {
            case 1:
                System.out.println("You Selected to play by yourself!");
                agent = new Human();
                game = new Game(agent);
        }
        StateRewardPair gameState = game.startRound();
        while (!gameState.state.isTerminal()){
            gameState.printState();
            gameState = game.step(gameState.state,agent.takeAction(gameState.state));
        }
        gameState.printState();
}
}
