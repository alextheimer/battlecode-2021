package player.handlers;

import battlecode.common.*;
import player.util.Flag;
import player.util.Util;
import player.util.UtilMath;
import player.util.Flag.AttackTargetFlag;
import player.util.Flag.EnemySightedFlag;
import player.util.Flag.FollowerClaimFlag;
import player.util.Flag.LeaderClaimFlag;
import player.util.Flag.OpCode;
import player.util.Flag.AssignmentFlag;
import player.util.UtilMath.DoubleVec2D;
import player.util.UtilMath.IntVec2D;
import player.util.UtilMath.Line2D;

import static player.handlers.HandlerCommon.*;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import player.handlers.LinearMoverHandler;

public class PoliticianHandler implements IRobotHandler {
	
	AssignmentType assignment = AssignmentType.UNASSIGNED;
	Optional<LinearMoverHandler> moveHandler = Optional.empty();
	
	private boolean attemptEmpower(RobotController rc, int radiusSquared) throws GameActionException {
		assert radiusSquared <= RobotType.POLITICIAN.actionRadiusSquared : "radius squared exceeds action radius";
		boolean empowerSuccessful;
		if (rc.canEmpower(radiusSquared)) {
			rc.empower(radiusSquared);
			empowerSuccessful = true;
		} else {
			empowerSuccessful = false;
		}
		System.out.println("empower attempt: " + empowerSuccessful);
		return empowerSuccessful;
	}
		
	private boolean attemptEmpowerNearestEnemy(RobotController rc) throws GameActionException {
		Optional<RobotInfo> enemyInfoOpt = HandlerCommon.senseNearestNonTeam(rc, rc.senseNearbyRobots());
		if (enemyInfoOpt.isPresent()) {
			RobotInfo enemyInfo = enemyInfoOpt.get();
			int enemyDistSquared = rc.getLocation().distanceSquaredTo(enemyInfo.getLocation());
			if (enemyDistSquared <= rc.getType().actionRadiusSquared) {
				return attemptEmpower(rc, enemyDistSquared);				
			}
		}
		return false;
	}
	
	private boolean patrolStep(RobotController rc) throws GameActionException {
		if (this.moveHandler.get().atEndOfLine() || this.moveHandler.get().blocked()) {
			this.moveHandler.get().reverse();
		}
		return this.moveHandler.get().step(rc);
	}
	
	private Optional<AssignmentFlag> findAssignmentFlag(RobotController rc) throws GameActionException {
		Optional<SimpleImmutableEntry<RobotInfo, Integer>> entryOpt = HandlerCommon.findFirstMatchingTeamFlag(
				rc,
				rc.senseNearbyRobots(HandlerCommon.MAX_DIST_SQUARED_ADJACENT, rc.getTeam()),
				(robotInfo, rawFlag) -> (Flag.getOpCode(rawFlag) == OpCode.ASSIGNMENT)
		);
		if (entryOpt.isPresent()) {
			return Optional.of(AssignmentFlag.decode(entryOpt.get().getValue()));
		} else {
			return Optional.empty();
		}
	}
	
	private void selfInitialize(RobotController rc) {
		Random rand = new Random();
		int degrees = rand.nextInt(UtilMath.MAX_DEGREES);
		MapLocation mapLoc = rc.getLocation();
		DoubleVec2D vec = UtilMath.degreesToVec(degrees);
		DoubleVec2D currCoord = new DoubleVec2D(mapLoc.x, mapLoc.y);
		this.assignment = AssignmentType.PATROL;
		this.moveHandler = Optional.of(new LinearMoverHandler(Line2D.make(vec, currCoord), vec));
	}
	
	private void flagInitialize(RobotController rc, AssignmentFlag flag) {
		this.assignment = flag.getAssignmentType();
		DoubleVec2D vec = UtilMath.degreesToVec(flag.getOutboundDegrees());
		DoubleVec2D origin = new DoubleVec2D(rc.getLocation().x, rc.getLocation().y);
		Line2D line = UtilMath.Line2D.make(vec, origin);
		this.moveHandler = Optional.of(new LinearMoverHandler(line, vec));
	}
	
	private void handleUnassigned(RobotController rc) throws GameActionException {
		Optional<AssignmentFlag> flagOpt = this.findAssignmentFlag(rc);
		if (flagOpt.isPresent()) {
			this.flagInitialize(rc, flagOpt.get());
			System.out.println("initialized via flag");
		} else {
			// not spawned by empowerment center
			this.selfInitialize(rc);
			System.out.println("initialized via self");
		}
		this.attemptEmpowerNearestEnemy(rc);
	}
	
	private Optional<RobotInfo> senseHighestPriorityNonTeammate(RobotController rc) {
		Function<RobotInfo, Double> costFunc = new Function<RobotInfo, Double>(){

			@Override
			public Double apply(RobotInfo robotInfo) {
				switch(robotInfo.getType()) {
					case POLITICIAN: return -1.0;
					case MUCKRAKER: return -2.0;
					case SLANDERER: return -3.0;
					case ENLIGHTENMENT_CENTER: return -4.0;
					default: throw new RuntimeException("unrecognized RobotInfo");
				}
			}
		};
		
		Collection<RobotInfo> sensedNonTeammates = HandlerCommon.senseAllNonTeam(rc);
		if (sensedNonTeammates.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(Util.findLeastCostLinear(sensedNonTeammates.iterator(), costFunc));
	}
	
	private void handlePatrol(RobotController rc) throws GameActionException {
		final int mask = (2 * HandlerCommon.MAX_WORLD_WIDTH) - 1;
		Optional<RobotInfo> calloutOpt = this.senseHighestPriorityNonTeammate(rc);
		if (calloutOpt.isPresent() && rc.canSetFlag(Flag.EMPTY_FLAG)) {
			RobotInfo robotInfo = calloutOpt.get();
			MapLocation mapLoc = robotInfo.getLocation(); 
			EnemySightedFlag flag = new EnemySightedFlag(robotInfo.getType(), mapLoc.x & mask, mapLoc.y & mask);
			rc.setFlag(flag.encode());
		}
		if (!this.attemptEmpowerNearestEnemy(rc)) {
			this.patrolStep(rc);
		}
	}
	
	@Override
	public IRobotHandler handle(RobotController rc) throws GameActionException {
		switch(this.assignment) {
			case UNASSIGNED:
				handleUnassigned(rc);
				break;
			case PATROL:
				handlePatrol(rc);
				break;
			default:
				throw new RuntimeException("Illegal AssignmentType: " + this.assignment);
		}
		return this;
	}

}
