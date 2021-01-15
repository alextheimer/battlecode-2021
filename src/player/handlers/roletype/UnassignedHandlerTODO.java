package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.SquadState;
import util.Flag;
import util.Util;
import util.Flag.LeaderClaimFlag;
import util.Flag.SquadAssignFlag;
import util.UtilMath;

import static player.handlers.HandlerCommon.*;

import java.util.Optional;

public class UnassignedHandlerTODO implements IRobotRoleHandler {

	private SquadState squadState;
	
	public UnassignedHandlerTODO(RobotController rc) throws GameActionException {
		this.squadState = new SquadState(RobotRole.UNASSIGNED, null);
		int leaderID = this.discernLeaderID(rc, this.squadState);
		this.squadState.orders.leaderID = leaderID;
		if (leaderID == rc.getID()) {
			// This bot is the squad leader; post the LeaderClaimFlag!
			LeaderClaimFlag flag = new LeaderClaimFlag();
			rc.setFlag(flag.encode());
		}
	}
	
	// TODO(theimer): should probably restrict radius
	private int getLeaderClaimFlagPosterID(RobotController rc) throws GameActionException  {
		for (RobotInfo robotInfo : rc.senseNearbyRobots()) {
			int id = robotInfo.getID();
			int flag = rc.getFlag(id);
			if (Flag.getOpCode(flag) == Flag.OpCode.LEADER_CLAIM) {
				return id;
			}
		}
		return NULL_ROBOT_ID;
	}
	
	private int discernLeaderID(RobotController rc, SquadState state) throws GameActionException {
		// Look for a nearby LeaderClaimFlag.
		int leaderID = getLeaderClaimFlagPosterID(rc);
		return leaderID != NULL_ROBOT_ID ? leaderID : rc.getID();
	}
	
	private Optional<SquadAssignFlag> squadAssignSearch(RobotController rc) throws GameActionException {
		for (RobotInfo robotInfo : rc.senseNearbyRobots()) {
			int id = robotInfo.getID();
			int rawFlag = rc.getFlag(id);
			if (Flag.getOpCode(rawFlag) == Flag.OpCode.SQUAD_ASSIGN) {
				return Optional.of(Flag.SquadAssignFlag.decode(rawFlag));
			}
		}
		return Optional.empty();
	}
	
	@Override
	public IRobotRoleHandler handle(RobotController rc) throws GameActionException {
		// TODO(theimer): all below -- this is gross
		// Look for orders from origin EnlightenmentCenter
		Optional<SquadAssignFlag> flagOpt = squadAssignSearch(rc);
		if (flagOpt.isPresent()) {
			// Orders found! Store the details...
			SquadAssignFlag flag = flagOpt.get();
			squadState.orders.squadType = flag.getSquadType();
			squadState.orders.pathVec = UtilMath.degreesToVec(flag.getOutboundDegrees());
			squadState.orders.pathLine = UtilMath.Line2D.make(squadState.orders.pathVec, new UtilMath.DoubleVec2D(rc.getLocation().x, rc.getLocation().y));
			// Note that this will cause RobotPlayer to instantiate a new handler.
			if (squadState.orders.leaderID == rc.getID()) {
				return new LeaderHandler(squadState);
			} else {
				return new FollowerHandler(squadState);
			}
		}
		return this;
	}
}
