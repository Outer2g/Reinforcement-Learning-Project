package utils;

import java.io.*;

/**
 * Created by outer2g on 18/06/17.
 */
public class Stats {
    public int wins;
    public int loses;
    public int gamesPlayed;
    public int hitsDealt;
    public int hitsReceived;
    public int damageDealt;
    public int damageReceived;
    public Stats(){
        resetStats();
    }
    public void resetStats(){
        wins = 0;
        loses = 0;
        gamesPlayed =0;
        hitsDealt = 0;
        hitsReceived = 0;
        damageDealt = 0;
        damageReceived = 0;
    }
    public void printStats(String path){
            PrintWriter output = null;
            try {
                output = new PrintWriter(new FileWriter(path,true));
                output.println("wins: " + wins );
                output.println("loses: " + loses );
                output.println("gamesPlayed: " + gamesPlayed);
                output.println("hitsDealt: " + hitsDealt );
                output.println("hitsReceived: " + hitsReceived );
                output.println("damageDealt: " + damageDealt );
                output.println("damageReceived: " + damageReceived );
                output.flush();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
}
