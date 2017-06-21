package ch.idsia.ai.agents.human;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 29, 2009
 * Time: 12:19:49 AM
 * Package: ch.idsia.ai.agents.ai;
 */
public class HumanKeyboardAgent extends KeyAdapter implements Agent
{
    List<boolean[]> history = new ArrayList<boolean[]>();
    private boolean[] Action = null;
    private String Name = "HumanKeyboardAgent";

    public HumanKeyboardAgent()
    {
        this.reset ();
//        RegisterableAgent.registerAgent(this);
    }

    public void reset()
    {
        // Just check you keyboard. Especially arrow buttons and 'A' and 'S'!
        Action = new boolean[Environment.numberOfButtons];
    }
    private byte[][] reduceSight(byte[][] world){
        int truncateX = 11;
        int truncateY = 7;
        byte ret[][] = new byte[world.length - truncateY][world[0].length - truncateX];

        for (int i = truncateY; i < world.length; i++) {
            for (int j = truncateX ; j < world[0].length; j++) {
                int finalPos[] = {i - truncateY, j - truncateX};
                ret[finalPos[0]][finalPos[1]] = world[i][j];
            }
        }
        return ret.clone();
    }
    public boolean[] getAction(Environment observation)
    {
        float[] enemiesPos = observation.getEnemiesFloatPos();
        byte world[][] = reduceSight(observation.getCompleteObservation());

//        world = observation.getEnemiesObservation();
//        System.out.println("length: " + world.length + "length2: " + world[0].length);
//        for (int i = 0; i < world.length; i++) {
//            for (int j = 0; j < world[0].length; j++) {
//                System.out.print(world[i][j] + " ");
//            }
//            System.out.println();
//        }
        System.out.println("Mario Pos: " + observation.getMarioFloatPos()[0] + " " + observation.getMarioFloatPos()[1]);
        return Action;
    }

    public AGENT_TYPE getType() {        return AGENT_TYPE.HUMAN;    }

    public String getName() {   return Name; }

    public void setName(String name) {        Name = name;    }


    public void keyPressed (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), true);
        System.out.println("sdf");
    }

    public void keyReleased (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), false);
    }


    private void toggleKey(int keyCode, boolean isPressed)
    {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                Action[Mario.KEY_LEFT] = isPressed;
                break;
            case KeyEvent.VK_RIGHT:
                Action[Mario.KEY_RIGHT] = isPressed;
                break;
            case KeyEvent.VK_DOWN:
                Action[Mario.KEY_DOWN] = isPressed;
                break;

            case KeyEvent.VK_S:
                Action[Mario.KEY_JUMP] = isPressed;
                break;
            case KeyEvent.VK_A:
                Action[Mario.KEY_SPEED] = isPressed;
                break;
        }
    }

   public List<boolean[]> getHistory () {
       return history;
   }
}
