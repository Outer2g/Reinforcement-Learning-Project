package utils;

import game.Action;

import static java.lang.Math.abs;

/**
 * Created by outer2g on 7/03/17.
 */
public class Stats {
    public int wins;
    public int loses;
    public int totalGames;
    public int totalActions;
    public int[] actions;
    public double winsLosesRatio;

    public Stats() {
        wins = 0;
        loses = 0;
        totalGames = 0;
        winsLosesRatio = 0.0;
        actions = new int[Action.NPOSSIBILITIES];
    }

    public void resetStats() {
        wins = 0;
        loses = 0;
        totalGames = 0;
        winsLosesRatio = 0.0;
    }

    public void updateRatio() {
        winsLosesRatio = ((double) wins) / loses;
        ++totalGames;
    }

    public void updateActions(int action) {
        ++this.actions[action];
        ++totalActions;
    }

    public void printStats() {
        System.out.println("Stats so far: ");
        System.out.println("Wins: " + wins);
        System.out.println("Loses: " + loses);
        System.out.println("W/L ratio: " + winsLosesRatio);
        System.out.println("Percentage: " + (((double) wins / totalGames) * 100));
        if (totalGames != 0){
        System.out.println("Percentage of not lost:" + (((double) ((totalGames - wins - loses) / totalGames) * 100)));
        System.out.println("Percentage of Hits: " + (((double) actions[Action.ACTION_HIT] / totalActions) * 100));}
    }
}