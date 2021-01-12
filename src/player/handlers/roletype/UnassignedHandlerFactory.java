package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;
import util.Flag;
import util.Util;

import static player.handlers.HandlerCommon.*;

class UnassignedHandler implements IRobotHandler {

	public UnassignedHandler() {
		// intentionally blank
	}
	
	// TODO(theimer): should probably restrict radius
	private int getLeaderClaimFlagPosterID(RobotInfo[] nearbyRobots, RobotController rc) throws GameActionException  {
		for (RobotInfo robotInfo : nearbyRobots) {
			int id = robotInfo.getID();
			int flag = rc.getFlag(id);
			if (Flag.getOpCode(flag) == Flag.OpCode.LEADER_CLAIM) {
				return id;
			}
		}
		return NULL_ROBOT_ID;
	}
	
	private void leaderSearch(RobotInfo[] nearbyRobots, RobotController rc,
			                  RobotState state) throws GameActionException {
		// Look for a nearby LeaderClaimFlag.
		int leaderID = getLeaderClaimFlagPosterID(nearbyRobots, rc);
		if (leaderID != NULL_ROBOT_ID) {
			state.orders.leaderID = leaderID;
			return;
		}
		
		// If no LeaderClaimFlag nearby, post a LeaderClaimFlag (this bot will be the leader).
		Flag.LeaderClaimFlag flag = new Flag.LeaderClaimFlag();
		rc.setFlag(flag.encode());
		state.orders.leaderID = rc.getID();
	}
	
	private void flagSearch(RobotInfo[] nearbyRobots, RobotController rc,
			                RobotState state) throws GameActionException {
		for (RobotInfo robotInfo : nearbyRobots) {
			int id = robotInfo.getID();
			int flag = rc.getFlag(id);
			if (Flag.getOpCode(flag) == Flag.OpCode.SQUAD_ASSIGN) {
				Flag.SquadAssignFlag squadAssignFlag = Flag.SquadAssignFlag.decode(flag);
				state.orders.squadType = squadAssignFlag.getSquadType();
				state.orders.outboundVec = Util.degreesToVec(squadAssignFlag.getOutboundDegrees());
				if (state.orders.leaderID == rc.getID()) {
					state.role = RobotRole.LEADER;
				} else {
					state.role = RobotRole.FOLLOWER;
				}
			}
		}
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
		// Store the leader-to-be (could be self)
		// TODO(theimer): move this to constructor?
		if (state.orders.leaderID == NULL_ROBOT_ID) {
			leaderSearch(nearbyRobots, rc, state);
		}
		// Look for orders from origin EnlightenmentCenter
		flagSearch(nearbyRobots, rc, state);
	}

}

public class UnassignedHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new UnassignedHandler();
	}

}
