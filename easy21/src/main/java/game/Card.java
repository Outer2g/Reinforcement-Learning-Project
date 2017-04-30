package game;

/**
 * Created by outer2g on 2/03/17.
 */
public class Card {
    private int number;
    private boolean isRed;
    public Card(int number,boolean isRed){
        this.isRed = isRed;
        this.number = number;
    }
    public Card(){}
    public int getCardNumber(){return this.number;}
    public boolean isCardRed(){return this.isRed;}
}
