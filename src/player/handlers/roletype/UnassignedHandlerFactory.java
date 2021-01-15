package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;
import util.Flag;
import util.Util;
import util.Flag.LeaderClaimFlag;
import util.Flag.SquadAssignFlag;
import util.UtilMath;

import static player.handlers.HandlerCommon.*;

import java.util.Optional;

class UnassignedHandlerTODO implements IRobotHandler {

	public UnassignedHandlerTODO(RobotController rc, RobotState state) throws GameActionException {
		int leaderID = this.discernLeaderID(rc, state);
		state.orders.leaderID = leaderID;
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
	
	private int discernLeaderID(RobotController rc, RobotState state) throws GameActionException {
		// Look for a nearby LeaderClaimFlag.
		int leaderID = getLeaderClaimFlagPosterID(rc);
		return leaderID != NULL_ROBOT_ID ? leaderID : rc.getID();
	}
	
	private Optional<SquadAssignFlag> squadAssignSearch(RobotController rc, RobotState state) throws GameActionException {
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
	public void handle(RobotController rc, RobotState state) throws GameActionException {
		// TODO(theimer): all below -- this is gross
		// Look for orders from origin EnlightenmentCenter
		Optional<SquadAssignFlag> flagOpt = squadAssignSearch(rc, state);
		if (flagOpt.isPresent()) {
			// Orders found! Store the details...
			SquadAssignFlag flag = flagOpt.get();
			state.orders.squadType = flag.getSquadType();
			state.orders.pathVec = UtilMath.degreesToVec(flag.getOutboundDegrees());
			state.orders.pathLine = UtilMath.Line2D.make(state.orders.pathVec, new UtilMath.DoubleVec2D(rc.getLocation().x, rc.getLocation().y));
			// Note that this will cause RobotPlayer to instantiate a new handler.
			if (state.orders.leaderID == rc.getID()) {
				state.role = RobotRole.LEADER;
			} else {
				state.role = RobotRole.FOLLOWER;
			}
		}
	}
}

public class UnassignedHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) throws GameActionException {
		return new UnassignedHandlerTODO(rc, state);
	}

}
