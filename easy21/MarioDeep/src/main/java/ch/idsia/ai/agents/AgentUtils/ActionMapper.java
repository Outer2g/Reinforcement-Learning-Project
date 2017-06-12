package ch.idsia.ai.agents.AgentUtils;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Created by outer2g on 25/05/17.
 */
public class ActionMapper {

    public static boolean[] performAction(Utils.ReducedActions action){
        boolean Action[] = new boolean[Environment.numberOfButtons];
        switch (action){
            case DOWN:
                Action[Mario.KEY_DOWN] = true;
                break;
            case JUMP:
                Action[Mario.KEY_JUMP] = true;
                break;
            case LEFT:
                Action[Mario.KEY_LEFT] = true;
                break;
            case RIGHT:
                Action[Mario.KEY_RIGHT] = true;
                break;
            case SPEED:
                Action[Mario.KEY_SPEED] = true;
                break;
            case LEFT_JUMP:
                Action[Mario.KEY_LEFT] = true;
                Action[Mario.KEY_JUMP] = true;
                break;
            case LEFT_SPEED:
                Action[Mario.KEY_LEFT] = true;
                Action[Mario.KEY_SPEED] = true;
                break;
            case RIGHT_JUMP:
                Action[Mario.KEY_RIGHT] = true;
                Action[Mario.KEY_JUMP] = true;
                break;
            case RIGHT_SPEED:
                Action[Mario.KEY_RIGHT] = true;
                Action[Mario.KEY_SPEED] = true;
                break;
            case DOWN_JUMP:
                Action[Mario.KEY_DOWN] = true;
                Action[Mario.KEY_JUMP] = true;
                break;
            default:
                break;
        }
        return Action;
    }

}
