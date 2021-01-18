package player.handlers.roletype;

import battlecode.common.*;
import util.Flag;
import util.Util;
import util.Flag.FollowerClaimFlag;
import util.Flag.LeaderClaimFlag;
import util.Flag.OpCode;
import util.Flag.SquadAssignFlag;
import util.UtilMath;
import util.UtilMath.DoubleVec2D;
import player.handlers.HandlerCommon;
import player.handlers.roletype.SquadState;

import static player.handlers.HandlerCommon.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.function.BiPredicate;

public class UnassignedHandlerTODO implements IRobotRoleHandler {
	
	private static final int FLAG_COOLDOWN_START = 3;  // current round, then 1 full round to propagate
	
	private SquadState.Builder squadStateBuilder;
	private int flagCooldown;
	private boolean ordersReceived;
	
	public UnassignedHandlerTODO(RobotController rc) throws GameActionException {
		this.flagCooldown = FLAG_COOLDOWN_START;
		this.ordersReceived = false;
		this.squadStateBuilder = new SquadState.Builder();
		int leaderID = this.discernLeaderID(rc);
		this.squadStateBuilder.setLeaderID(leaderID);
		// TODO(theimer): make a Flag interface with encode.
		if (leaderID == rc.getID()) {
			// This bot is the squad leader; post the LeaderClaimFlag!
			LeaderClaimFlag flag = new LeaderClaimFlag();
			rc.setFlag(flag.encode());
		} else {
			// This bot is a follower; post the FollowerClaimFlag!
			FollowerClaimFlag flag = new FollowerClaimFlag();
			rc.setFlag(flag.encode());
		}
	}
	

	
	private int discernLeaderID(RobotController rc) throws GameActionException {
		// Look for a nearby LeaderClaimFlag.
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
		BiPredicate<RobotInfo, Integer> predicate = (robotInfo, rawFlag) -> (Flag.getOpCode(rawFlag) == Flag.OpCode.LEADER_CLAIM);
		Optional<SimpleImmutableEntry<RobotInfo, Integer>> entryOptional = HandlerCommon.findFirstMatchingFlag(rc, nearbyRobots, predicate);
		return entryOptional.isPresent() ? entryOptional.get().getKey().getID() : rc.getID();
	}
	
	private Optional<SquadAssignFlag> squadAssignSearch(RobotController rc) throws GameActionException {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
		BiPredicate<RobotInfo, Integer> predicate = (robotInfo, rawFlag) -> (Flag.getOpCode(rawFlag) == Flag.OpCode.SQUAD_ASSIGN);
		Optional<SimpleImmutableEntry<RobotInfo, Integer>> entryOptional = HandlerCommon.findFirstMatchingFlag(rc, nearbyRobots, predicate);
		return entryOptional.isPresent() ? Optional.of(Flag.SquadAssignFlag.decode(entryOptional.get().getValue())) : Optional.empty();
		
	}
	
	private static Set<Integer> collectSquadIdSet(RobotController rc) throws GameActionException {
		RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
		BiPredicate<RobotInfo, Integer> predicate = new BiPredicate<RobotInfo, Integer>() {
			@Override
			public boolean test(RobotInfo robotInfo, Integer rawFlag) {
				Flag.OpCode opCode = Flag.getOpCode(rawFlag);
				return ((opCode == OpCode.FOLLOWER_CLAIM) || (opCode == OpCode.LEADER_CLAIM));
			}
		};
		Map<RobotInfo, Integer> matchingFlagMap = HandlerCommon.findAllMatchingFlags(rc, nearbyRobots, predicate);
		Set<Integer> squadIdSet = Util.legalSetCollect(matchingFlagMap.keySet().stream().map(robotInfo -> robotInfo.getID()));
		squadIdSet.add(rc.getID());
		return squadIdSet;
	}
	
	@Override
	public IRobotRoleHandler handle(RobotController rc) throws GameActionException {
		// TODO(theimer): all below -- this is gross
		if (!this.ordersReceived) {
			// Look for orders from origin EnlightenmentCenter
			Optional<SquadAssignFlag> flagOpt = squadAssignSearch(rc);
			if (flagOpt.isPresent()) {
				this.ordersReceived = true;
				// Orders found! Store the details...
				SquadAssignFlag flag = flagOpt.get();
				DoubleVec2D pathVec = UtilMath.degreesToVec(flag.getOutboundDegrees());
				
				this.squadStateBuilder
					.setSquadType(flag.getSquadType())
					.setPathVec(pathVec)
					.setPathLine(UtilMath.Line2D.make(pathVec, new UtilMath.DoubleVec2D(rc.getLocation().x, rc.getLocation().y)));
				
				Set<Integer> squadIdSet = collectSquadIdSet(rc);
				this.squadStateBuilder.setSquadIdSet(squadIdSet);
			}
		}
		

		if (this.ordersReceived) {
			this.flagCooldown--;
		}
		
		IRobotRoleHandler nextHandler;
		if (this.flagCooldown == 0) {
			rc.setFlag(Flag.EMPTY_FLAG);
			SquadState squadState = this.squadStateBuilder.build();
			if (squadState.leaderID == rc.getID()) {
				nextHandler = new LeaderHandler(squadState);
			} else {
				nextHandler = new FollowerHandler(squadState);
			}				
		} else {
			nextHandler = this;			
		}
		
		return nextHandler;
	}
}
