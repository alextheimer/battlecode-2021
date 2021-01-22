package player.handlers;

import battlecode.common.*;
import player.util.Flag;
import player.util.Util;
import player.util.UtilMath;
import player.util.Flag.AttackTargetFlag;
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
		HandlerCommon.battlecodeAssert(radiusSquared <= RobotType.POLITICIAN.actionRadiusSquared,
				                       "radius squared exceeds action radius", rc);
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
		if (this.moveHandler.get().atEndOfLine()) {
			this.moveHandler.get().reverse();
		}
		return this.moveHandler.get().step(rc);
	}
	
	private Optional<AssignmentFlag> findAssignmentFlag(RobotController rc) throws GameActionException {
		Optional<SimpleImmutableEntry<RobotInfo, Integer>> entryOpt = HandlerCommon.findFirstMatchingFlag(
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
	
	private void handlePatrol(RobotController rc) throws GameActionException {
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
				HandlerCommon.battlecodeAssert(false, "leaders shouldn't have these SquadTypes!", rc);
		}
		return this;
	}

}
