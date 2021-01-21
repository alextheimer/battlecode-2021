package player.handlers;

import battlecode.common.*;
import player.util.Flag;
import player.util.Util;
import player.util.Flag.FollowerClaimFlag;
import player.util.Flag.LeaderClaimFlag;
import player.util.Flag.OpCode;
import player.util.Flag.SquadAssignFlag;
import player.util.UtilMath;
import player.util.UtilMath.DoubleVec2D;
import player.handlers.SquadState;

import static player.handlers.HandlerCommon.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.function.BiPredicate;

public class UnassignedHandlerTODO implements IRobotRoleHandler {
	
	private SquadState.Builder squadStateBuilder;
	private Queue<StageHandler> stageHandlerQueue;
	
	private interface StageHandler {
		public void handle(RobotController rc) throws GameActionException;
		public boolean isComplete();
	}
	
	private class OrdersStageHandler implements StageHandler {
		
		private boolean ordersReceived = false;
		
		@Override
		public void handle(RobotController rc) throws GameActionException {
			// Look for orders from origin EnlightenmentCenter
			Optional<SquadAssignFlag> flagOpt = UnassignedHandlerTODO.this.squadAssignSearch(rc);
			if (flagOpt.isPresent()) {
				this.ordersReceived = true;
				// Orders found! Store the details...
				SquadAssignFlag flag = flagOpt.get();
				DoubleVec2D pathVec = UtilMath.degreesToVec(flag.getOutboundDegrees());
	
				UnassignedHandlerTODO.this.squadStateBuilder
					.setSquadType(flag.getSquadType())
					.setPathVec(pathVec)
					.setPathLine(UtilMath.Line2D.make(pathVec, new UtilMath.DoubleVec2D(rc.getLocation().x, rc.getLocation().y)));
			}
		}

		@Override
		public boolean isComplete() {
			return this.ordersReceived;
		}
	}
	
	private class SquadStageHandler implements StageHandler {
		
		private int waitCountdown = 3;
		
		@Override
		public void handle(RobotController rc) throws GameActionException {
			waitCountdown--;
			if (waitCountdown == 0) {
				Set<Integer> squadIdSet = UnassignedHandlerTODO.collectSquadIdSet(rc);
				System.out.println("found in squad: " + squadIdSet.size());
				HandlerCommon.battlecodeAssert(squadIdSet.size() > 1, "SQUAD TOO SMALL", rc);
				UnassignedHandlerTODO.this.squadStateBuilder.setSquadIdSet(squadIdSet);				
			}
		}

		@Override
		public boolean isComplete() {
			return this.waitCountdown == 0;
		}
	}
	
	private class FlagPropHandler implements StageHandler {
		
		private int waitCountdown = 3;
		
		@Override
		public void handle(RobotController rc) throws GameActionException {
			waitCountdown--;
		}

		@Override
		public boolean isComplete() {
			return this.waitCountdown == 0;
		}
	}
	
	public UnassignedHandlerTODO(RobotController rc) throws GameActionException {
		this.stageHandlerQueue = new ArrayDeque<StageHandler>();
		
		this.stageHandlerQueue.add(new OrdersStageHandler());
		this.stageHandlerQueue.add(new SquadStageHandler());
		this.stageHandlerQueue.add(new FlagPropHandler());
		
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
		
		while (!this.stageHandlerQueue.isEmpty()) {
			StageHandler stageHandler = this.stageHandlerQueue.peek();
			stageHandler.handle(rc);
			if (stageHandler.isComplete()) {
				this.stageHandlerQueue.remove();
			} else {
				break;
			}
		}
		
		IRobotRoleHandler nextHandler;
		if (this.stageHandlerQueue.isEmpty()) {
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
