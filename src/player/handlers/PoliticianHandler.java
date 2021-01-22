package player.handlers;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotRoleHandler;
import player.util.Flag;
import player.util.Util;
import player.util.UtilMath;
import player.util.Flag.AttackTargetFlag;
import player.util.Flag.FollowerClaimFlag;
import player.util.Flag.LeaderClaimFlag;
import player.util.Flag.OpCode;
import player.util.Flag.SquadAssignFlag;
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
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import player.handlers.LinearMoverHandler;

public class PoliticianHandler implements IRobotTypeHandler {
	
	Optional<SquadType> role = Optional.of(SquadType.UNASSIGNED);
	Optional<LinearMoverHandler> moveHandler = Optional.empty();
	
	private boolean attemptEmpower(RobotController rc, int radiusSquared) throws GameActionException {
		HandlerCommon.battlecodeAssert(radiusSquared <= RobotType.POLITICIAN.actionRadiusSquared, "radius squared exceeds action radius", rc);
		if (rc.canEmpower(radiusSquared)) {
			System.out.println("successful empower");
			rc.empower(radiusSquared);
			return true;
		} else {
			System.out.println("failed empower");
			return false;
		}
	}
	
	private boolean attemptEmpowerNearestEnemy(RobotController rc) throws GameActionException {
		Optional<RobotInfo> enemyInfoOpt = HandlerCommon.findNearestEnemy(rc, rc.senseNearbyRobots());
		boolean empowerSuccessful;
		if (enemyInfoOpt.isPresent()) {
			RobotInfo enemyInfo = enemyInfoOpt.get();
			int enemyDistSquared = rc.getLocation().distanceSquaredTo(enemyInfo.getLocation());
			if (enemyDistSquared <= rc.getType().actionRadiusSquared) {
				empowerSuccessful = attemptEmpower(rc, enemyDistSquared);				
			} else {
				empowerSuccessful = false;
			}
		} else {
			empowerSuccessful = false;
		}
		return empowerSuccessful;
	}
	
	private void patrolStep(RobotController rc) throws GameActionException {
		if (!this.moveHandler.get().step(rc)) {
			this.moveHandler.get().reverse();
			this.moveHandler.get().step(rc);
		}
	}
	
	private void handleUnassigned(RobotController rc) throws GameActionException {
		Optional<SimpleImmutableEntry<RobotInfo, Integer>> entryOpt = HandlerCommon.findFirstMatchingFlag(
				rc,
				rc.senseNearbyRobots(RobotType.POLITICIAN.sensorRadiusSquared, rc.getTeam()),
				(robotInfo, rawFlag) -> (Flag.getOpCode(rawFlag) == OpCode.SQUAD_ASSIGN)
		);
		if (entryOpt.isPresent()) {
			int rawFlag = entryOpt.get().getValue();
			SquadAssignFlag flag = SquadAssignFlag.decode(rawFlag);
			this.role = Optional.of(flag.getSquadType());
			DoubleVec2D vec = UtilMath.degreesToVec(flag.getOutboundDegrees());
			DoubleVec2D origin = new DoubleVec2D(rc.getLocation().x, rc.getLocation().y);
			Line2D line = UtilMath.Line2D.make(vec, origin);
			this.moveHandler = Optional.of(new LinearMoverHandler(line, vec));
		} else {
			this.attemptEmpowerNearestEnemy(rc);
		}
	}
	
	private void handlePatrol(RobotController rc) throws GameActionException {
		if (!this.attemptEmpowerNearestEnemy(rc)) {
			this.patrolStep(rc);
		}
	}
	
	@Override
	public IRobotTypeHandler handle(RobotController rc) throws GameActionException {
		switch(this.role.get()) {
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
