package player.handlers.roletype;

import battlecode.common.*;
import util.Flag;
import util.Util;
import util.Flag.LeaderClaimFlag;
import util.Flag.SquadAssignFlag;
import util.UtilMath;
import util.UtilMath.DoubleVec2D;
import player.handlers.roletype.SquadState;

import static player.handlers.HandlerCommon.*;

import java.util.HashSet;
import java.util.Optional;

public class UnassignedHandlerTODO implements IRobotRoleHandler {

	private SquadState.Builder squadStateBuilder;
	
	public UnassignedHandlerTODO(RobotController rc) throws GameActionException {
		this.squadStateBuilder = new SquadState.Builder();
		int leaderID = this.discernLeaderID(rc);
		this.squadStateBuilder.setLeaderID(leaderID);
		if (leaderID == rc.getID()) {
			// This bot is the squad leader; post the LeaderClaimFlag!
			LeaderClaimFlag flag = new LeaderClaimFlag();
			rc.setFlag(flag.encode());
		}
	}
	
	// TODO(theimer): should probably restrict radius
	private Optional<Integer> getLeaderClaimFlagPosterIdOpt(RobotController rc) throws GameActionException  {
		for (RobotInfo robotInfo : rc.senseNearbyRobots()) {
			int id = robotInfo.getID();
			int flag = rc.getFlag(id);
			if (Flag.getOpCode(flag) == Flag.OpCode.LEADER_CLAIM) {
				return Optional.of(id);
			}
		}
		return Optional.empty();
	}
	
	private int discernLeaderID(RobotController rc) throws GameActionException {
		// Look for a nearby LeaderClaimFlag.
		Optional<Integer> leaderIdOpt = getLeaderClaimFlagPosterIdOpt(rc);
		return leaderIdOpt.isPresent() ? leaderIdOpt.get() : rc.getID();
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
			DoubleVec2D pathVec = UtilMath.degreesToVec(flag.getOutboundDegrees());
			
			this.squadStateBuilder
				.setSquadType(flag.getSquadType())
				.setPathVec(pathVec)
				.setPathLine(UtilMath.Line2D.make(pathVec, new UtilMath.DoubleVec2D(rc.getLocation().x, rc.getLocation().y)));
			
			// TODO(theimer): placeholder!!!!
			this.squadStateBuilder.setSquadIdSet(new HashSet<>());
			
			SquadState squadState = this.squadStateBuilder.build();
			
			if (squadState.leaderID == rc.getID()) {
				return new LeaderHandler(squadState);
			} else {
				return new FollowerHandler(squadState);
			}
		}
		return this;
	}
}
