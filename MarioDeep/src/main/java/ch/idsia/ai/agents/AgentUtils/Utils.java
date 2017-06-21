package ch.idsia.ai.agents.AgentUtils;

/**
 * Created by outer2g on 23/05/17.
 */
public class Utils {
    public static void consoleLog(String message){
//        System.out.println(message);

    }
    public static void printMatrix(byte[][] matrix){
        consoleLog("length: " + matrix.length + "length2: " + matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            consoleLog("");
        }
    }
    public enum ReducedActions {
        RIGHT, LEFT, DOWN, JUMP, SPEED,RIGHT_JUMP,LEFT_JUMP,DOWN_JUMP,RIGHT_SPEED,LEFT_SPEED
    }
}
