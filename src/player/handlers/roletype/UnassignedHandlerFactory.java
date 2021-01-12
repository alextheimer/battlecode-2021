package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;
import util.Flag;

import static player.handlers.HandlerCommon.*;

class UnassignedHandler implements IRobotHandler {

	public UnassignedHandler() {
		// intentionally blank
	}
	
	// TODO(theimer): should probably restrict radius
	private int getLeaderClaimFlagPosterID(RobotController rc) throws GameActionException {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
		for (RobotInfo robotInfo : nearbyRobots) {
			int id = robotInfo.getID();
			int flag = rc.getFlag(id);
			if (Flag.getOpCode(flag) == Flag.OpCode.LEADER_CLAIM) {
				return id;
			}
		}
		return NULL_ROBOT_ID;
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
		// If already assigned a leader, just standby for orders.
		if (state.orders.leaderID != NULL_ROBOT_ID) {
			return;
		}
		
		// Look for a nearby LeaderClaimFlag.
		int leaderID = getLeaderClaimFlagPosterID(rc);
		if (leaderID != NULL_ROBOT_ID) {
			state.orders.leaderID = leaderID;
			return;
		}
		
		// If no LeaderClaimFlag nearby, post a LeaderClaimFlag (this bot will be the leader).
		Flag.LeaderClaimFlag flag = new Flag.LeaderClaimFlag();
		rc.setFlag(flag.encode());
		state.orders.leaderID = rc.getID();
	}

}

public class UnassignedHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new UnassignedHandler();
	}

}
