package game;


import Agents.Agent;
import Agents.MCAgent;

import java.util.Random;

/**
 * Created by outer2g on 2/03/17.
 */
public class Game {
    public static final int DEALER = 0;
    public static final int PLAYER = 1;
    public static final int DRAW = 2;
    private int dealersHand;
    private int playersHand;
    private Random rng;
    private Agent currentAgent;
    public Game(Agent player){
        this.dealersHand = 0;
        this.playersHand = 0;
        this.rng = new Random();
        this.currentAgent = player;
    }
    public StateRewardPair startRound(){
        Card dealerCard,playerCard;
        while(!(dealerCard = drawCard()).isCardRed());
        while(!(playerCard = drawCard()).isCardRed());
        dealersHand += dealerCard.getCardNumber();
        playersHand += playerCard.getCardNumber();
        return new StateRewardPair(new State(dealerCard.getCardNumber(),playersHand),0);
    }
    public StateRewardPair step(State previousState,Action action){
        if (action.getAction() == Action.ACTION_HIT) hit(Game.PLAYER);
        else {
            while (dealersHand <= 17){
                hit(Game.DEALER);
                if (dealersHand > 21 || dealersHand < 1){
                    State state = new State(dealersHand,playersHand,true,Game.PLAYER);//dealer goes bust
                    return new StateRewardPair(state,1);
                    }
            }
            if (dealersHand == playersHand) return new StateRewardPair(
                    new State(dealersHand,playersHand,true,Game.DRAW),0);
            return new StateRewardPair(
                    new State(dealersHand,playersHand,true,
                            dealersHand > playersHand ? Game.DEALER : Game.PLAYER),
                    dealersHand > playersHand ? -1 : 1);
        }
        if (playersHand > 21 || playersHand < 1)
            return new StateRewardPair(
                    new State(dealersHand,playersHand,true,Game.DEALER),-1);//player busts => dealer wins
        return new StateRewardPair(new State(dealersHand,playersHand),0);

    }
    // 0 - dealer wins, 1 - player wins
    /*public int startRound(){
        //drawing a black card for both players
        Card dealerCard,playerCard;
        while(!(dealerCard = drawCard()).isCardRed());
        while(!(playerCard = drawCard()).isCardRed());
        dealersHand += dealerCard.getCardNumber();
        playersHand += playerCard.getCardNumber();
        State currentState = new State(dealerCard.getCardNumber(),playersHand);
        while (currentAgent.takeAction(currentState).getAction() == Action.ACTION_HIT){
            //player wants to hit
            hit(Game.PLAYER);
            if (playersHand > 21 || playersHand < 1) return Game.DEALER;//player busts => dealer wins
            currentState = new State(dealerCard.getCardNumber(),playersHand);
        }
        while (dealersHand <= 17){
            hit(Game.DEALER);
            if (dealersHand > 21 || dealersHand < 1) return Game.PLAYER;//dealer goes bust
        }
        if (dealersHand == playersHand) return Game.DRAW;
        return dealersHand > playersHand ? Game.DEALER : Game.PLAYER;
    }*/
    private void hit(int hitter){
        Card drawn = drawCard();
        if (hitter == Game.PLAYER){
            updatePlayer(drawn);
        }
        else{
            updateDealer(drawn);
        }
    }
    private void updatePlayer(Card card){
        if (card.isCardRed()) playersHand -= card.getCardNumber();
        else playersHand += card.getCardNumber();
    }
    private void updateDealer(Card card){
        if (card.isCardRed()) dealersHand -= card.getCardNumber();
        else dealersHand += card.getCardNumber();
    }
    private Card drawCard(){
        //since random.nextInt(n) gives inclusive 0 and exclusive n, we need to sum 1.
        int number = rng.nextInt(10) + 1;
        boolean isRed = rng.nextInt(3) == 0; // probability of being red = 1/3
        return new Card(number,isRed);
    }
}
