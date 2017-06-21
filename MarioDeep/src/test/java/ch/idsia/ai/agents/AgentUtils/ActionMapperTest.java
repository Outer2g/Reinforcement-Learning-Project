package ch.idsia.ai.agents.AgentUtils;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.*;

/**
 * Created by outer2g on 25/05/17.
 */
public class ActionMapperTest {
    @Test
    public void testPerformDownAction() throws Exception {
        boolean[] action = ActionMapper.performAction(Utils.ReducedActions.DOWN);
        assert action[Mario.KEY_DOWN];
    }
    @Test
    public void testPerformActionShouldReturnLastActionDone() throws Exception{
        boolean[] action = ActionMapper.performAction(Utils.ReducedActions.LEFT_SPEED);
        assert action[Mario.KEY_SPEED] && action[Mario.KEY_LEFT];
        action = ActionMapper.performAction(Utils.ReducedActions.JUMP);
        assert action[Mario.KEY_JUMP] && !action[Mario.KEY_SPEED] && !action[Mario.KEY_LEFT];

    }

}