package player.handlers.roletype;

import battlecode.common.*;
import static player.handlers.HandlerCommon.*;
import util.Util;
import util.UtilMath;

public class FollowerHandler implements IRobotRoleHandler {

	public FollowerHandler() {
		// blank
	}
	
	private static MapLocation getLeaderCoord(RobotController rc, RobotState state) throws GameActionException {
		RobotInfo leaderInfo = rc.senseRobot(state.orders.leaderID);
		return leaderInfo.getLocation();
	}
	
	@Override
	public IRobotRoleHandler handle(RobotController rc, RobotState state) throws GameActionException {
		MapLocation robotLocation = rc.getLocation();
		MapLocation leaderLocation = getLeaderCoord(rc, state);
		
		Direction dir = Util.directionToGoal(robotLocation, leaderLocation);

		System.out.println("Want to move: " + dir);
		rc.move(dir);
		System.out.println("MOVE SUCCESSFUL");
//		if (rc.canMove(dir)) {
//			System.out.println("Ayyyyyyyyy");
//			rc.move(dir);
//		} else {
//			System.out.println("large sad");
//		}
		return this;
	}
	
}
