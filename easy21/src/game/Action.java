package game;

/**
 * Created by outer2g on 2/03/17.
 */
public class Action {
    //definitions
    public static final int ACTION_HIT = 0;
    public static final int ACTION_STICK = 1;
    private int actionTotake;
    public Action(int action){
        this.actionTotake = action;
    }
    public int getAction(){
        return this.actionTotake;
    }
}
