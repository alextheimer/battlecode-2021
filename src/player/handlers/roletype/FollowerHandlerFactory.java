package player.handlers.roletype;

import battlecode.common.*;
import static player.handlers.HandlerCommon.*;
import util.Util;

class FollowerHandler implements IRobotHandler {

	public FollowerHandler() {
		// blank
	}
	
	private static MapLocation getLeaderCoord(RobotController rc, RobotState state) throws GameActionException {
		RobotInfo leaderInfo = rc.senseRobot(state.orders.leaderID);
		return leaderInfo.getLocation();
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
		MapLocation robotLocation = rc.getLocation();
		MapLocation leaderLocation = getLeaderCoord(rc, state);
		
		Direction dir = Util.directionToGoal(
				new Util.DoubleVec2D((double)robotLocation.x, (double)robotLocation.y),
				new Util.DoubleVec2D((double)leaderLocation.x, (double)leaderLocation.y));
		if (rc.canMove(dir)) {
			rc.move(dir);
		}
	}
	
}

public class FollowerHandlerFactory implements IRobotHandlerFactory {
	
	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new FollowerHandler();
	}

}
