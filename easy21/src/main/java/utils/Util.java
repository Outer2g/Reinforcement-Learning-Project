package utils;

import Agents.*;

/**
 * Created by outer2g on 26/04/17.
 */
public class Util {
    public static Agent getAgent(String agent, double alpha, double gamma){

        if (agent.equals("SARSA")) return new SARSAgent(alpha,gamma);
        else if (agent.equals("Q-Learning")) return new QLAgent(alpha,gamma);
        else if (agent.equals("NNQ-Learning")) return new NNQLAgent(alpha,gamma);
        else if (agent.equals("LFAQ-Learning")) return new LFAQLAgent(alpha,gamma);
        else return new SARSAgent(alpha,gamma);
    }
}
