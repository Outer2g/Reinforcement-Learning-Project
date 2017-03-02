package game;

/**
 * Created by outer2g on 2/03/17.
 */
public class State {
    private int dealersCard;
    private int playerSum;
    private boolean isTerminal;
    private int winner;
    public State(int dealer,int player){
        this.dealersCard = dealer;
        this.playerSum = player;
        this.isTerminal = false;
    }
    public State(int dealer,int player,boolean isTerminal,int winner){
        this.dealersCard = dealer;
        this.playerSum = player;
        this.isTerminal = isTerminal;
        this.winner = winner;
    }
    public int getDealersCard(){
        return this.dealersCard;
    }
    public int getPlayerSum(){
        return this.playerSum;
    }
    public boolean isTerminal(){return this.isTerminal;}
    public int getWinner(){return this.winner;}
    public void printState(){
        System.out.println("Dealer Has: " + getDealersCard());
        System.out.println("You have: " + getPlayerSum());
        if (isTerminal){
            if(winner == Game.PLAYER)System.out.println("you win!");
            else if(winner == Game.DEALER)System.out.println("dealer wins!");
            else System.out.println("Draw!");
        }
    }
}
