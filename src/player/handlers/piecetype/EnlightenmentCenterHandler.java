package player.handlers.piecetype;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import player.handlers.HandlerCommon;

public class EnlightenmentCenterHandler {
	
	private static enum Squad {PATROL, STRIKE};
	
	private static Map<Squad, List<RobotType>> squadSpecMap = new HashMap<>();
	static {
		squadSpecMap.put(Squad.PATROL, Arrays.asList(RobotType.POLITICIAN, RobotType.POLITICIAN));
	}
	
    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    private static RobotType randomSpawnableRobotType() {
        return HandlerCommon.spawnableRobot[(int) (Math.random() * HandlerCommon.spawnableRobot.length)];
    }
    
	public static void handle(RobotController rc, HandlerCommon.RobotState state) throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : HandlerCommon.directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
            } else {
                break;
            }
        }
	}
}
