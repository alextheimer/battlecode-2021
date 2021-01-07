package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.IRobotHandlerFactory;
import player.handlers.HandlerCommon.RobotState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static player.handlers.HandlerCommon.*;

class EnlightenmentCenterHandler implements IRobotHandler {
	
	private static Map<SquadType, List<RobotType>> squadSpecMap = new HashMap<>();
	static {
		squadSpecMap.put(SquadType.PATROL, Arrays.asList(RobotType.POLITICIAN, RobotType.POLITICIAN));
	}
	
    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    private static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }
    
//    private static SquadType chooseSquadType(RobotController rc) {
//    	return SquadType.PATROL;
//    }
	
	public EnlightenmentCenterHandler() {
		// blank
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
    	RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
            } else {
                break;
            }
        }
	}


}

public class EnlightenmentCenterHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new EnlightenmentCenterHandler();
	}

}
