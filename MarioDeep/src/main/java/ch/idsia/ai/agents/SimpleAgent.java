package ch.idsia.ai.agents;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: May 12, 2009
 * Time: 7:28:57 PM
 * Package: ch.idsia.ai.agents
 */
public class SimpleAgent implements Agent
{
    protected boolean Action[] = new boolean[Environment.numberOfButtons];
    protected String Name = "SimpleAgent";

    public void reset()
    {
        Action = new boolean[Environment.numberOfButtons];
        Action[Mario.KEY_RIGHT] = true;
        Action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction(Environment observation)
    {
        byte world[][] = observation.getCompleteObservation();
        System.out.println("length: " + world.length + "length2: " + world.length);
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world.length; j++) {
                System.out.print(world[i][j] + " ");
            }
            System.out.println();
        }
        int marioX = (int) observation.getMarioFloatPos()[0];
        int marioY = (int) observation.getMarioFloatPos()[1];
        System.out.println("Mario pos: " + marioX + " " + marioY);
        System.out.println("Level observation: " + observation.getBitmapLevelObservation());

//        System.out.println("World pos in mario: " + world[marioX][marioY]);
        Action[Mario.KEY_DOWN] = true;
        return Action;
    }

    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
    }

    public String getName() {        return Name;    }

    public void setName(String Name) { this.Name = Name;    }
}
