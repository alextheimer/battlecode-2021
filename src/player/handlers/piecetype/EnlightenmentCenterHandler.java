package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon;

public class EnlightenmentCenterHandler {
    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return HandlerCommon.spawnableRobot[(int) (Math.random() * HandlerCommon.spawnableRobot.length)];
    }
    
	public static void handle(RobotController rc) throws GameActionException {
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
