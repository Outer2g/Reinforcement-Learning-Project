package Agents;

import game.Action;
import game.State;

import java.util.Scanner;

/**
 * Created by outer2g on 2/03/17.
 */
public class Human extends Agent {
    public Human(){}
    @Override
    public Action takeAction(State state) {
        System.out.println("Print an action to make");
        System.out.println(Action.ACTION_STICK + ": to stick \n" +
                Action.ACTION_HIT + ":to hit");
        int action = new Scanner(System.in).nextInt();
        return new Action(action);
    }
}
