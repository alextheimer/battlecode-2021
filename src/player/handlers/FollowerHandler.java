package player.handlers;

import battlecode.common.*;
import static player.handlers.HandlerCommon.*;

import player.util.UtilMath;

public class FollowerHandler implements IRobotRoleHandler {

	private SquadState squadState;
	
	public FollowerHandler(SquadState squadState) {
		this.squadState = squadState;
	}
	
	private MapLocation getLeaderCoord(RobotController rc) throws GameActionException {
		RobotInfo leaderInfo = rc.senseRobot(this.squadState.leaderID);
		return leaderInfo.getLocation();
	}
	
	@Override
	public IRobotRoleHandler handle(RobotController rc) throws GameActionException {
		MapLocation robotLocation = rc.getLocation();
		MapLocation leaderLocation = getLeaderCoord(rc);
		
		Direction dir = HandlerCommon.directionToGoal(robotLocation, leaderLocation);

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
