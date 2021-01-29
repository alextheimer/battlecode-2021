package player.handlers.common;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import battlecode.common.*;
import player.handlers.common.HandlerCommon;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.general.UtilGeneral;
import player.util.math.IntVec2D;
import player.util.math.UtilMath;
import player.util.math.UtilMath.*;

/**
 * Contains functions common among handlers.
 */
public class HandlerCommon {
	
	public static boolean attemptMove(RobotController rc, Direction dir) throws GameActionException {
		UtilBattlecode.log("Want to move: " + dir);
		if (rc.canMove(dir)) {
			rc.move(dir);
			UtilBattlecode.log("Move successful!");
			return true;
		} else {
			UtilBattlecode.log("Move failed!");
			return false;
		}
	}
    
	// TODO(theimer): remove team check from all uses
	public static Map<RobotInfo, Integer> findAllMatchingTeamFlags(RobotController rc, RobotInfo[] nearbyRobots,
	                                   BiPredicate<RobotInfo, Integer> predicate) throws GameActionException  {
		Map<RobotInfo, Integer> robotFlagMap = new HashMap<>();
		for (RobotInfo robotInfo : nearbyRobots) {
			int rawFlag = rc.getFlag(robotInfo.getID());
			if ((robotInfo.getTeam() == rc.getTeam()) && predicate.test(robotInfo, rawFlag)) {
				robotFlagMap.put(robotInfo, rawFlag);
			}
		}
		return robotFlagMap;
	}
	
	// TODO(theimer): remove team check from all uses
	public static Optional<SimpleImmutableEntry<RobotInfo, Integer>> findFirstMatchingTeamFlag(RobotController rc, RobotInfo[] nearbyRobots,
                                       BiPredicate<RobotInfo, Integer> predicate) throws GameActionException  {
		for (RobotInfo robotInfo : nearbyRobots) {
			int rawFlag = rc.getFlag(robotInfo.getID());
			if ((robotInfo.getTeam() == rc.getTeam()) && predicate.test(robotInfo, rawFlag)) {
				return Optional.of(new SimpleImmutableEntry<RobotInfo, Integer>(robotInfo, rawFlag));
			}
		}
		return Optional.empty();
	}

	public static Optional<RobotInfo> senseNearestNonTeam(RobotController rc, RobotInfo[] nearbyRobots) {
		Iterator<RobotInfo> otherTeamIterator = Arrays.stream(nearbyRobots).filter(robotInfo -> robotInfo.getTeam() != rc.getTeam()).iterator();
		if (otherTeamIterator.hasNext()) {
			Function<RobotInfo, Double> costFunc = robotInfo -> (double)robotInfo.getLocation().distanceSquaredTo(rc.getLocation());
			return Optional.of(UtilGeneral.findLeastCostLinear(otherTeamIterator, costFunc));
		} else {
			return Optional.empty();
		}
	}
	
	public static Set<RobotInfo> senseAllNonTeam(RobotController rc) {
		return UtilGeneral.legalSetCollect(Arrays.stream(rc.senseNearbyRobots()).filter(robotInfo -> robotInfo.getTeam() != rc.getTeam()));
	}
	
	public static Set<RobotInfo> senseAllTeam(RobotController rc) {
		return UtilGeneral.legalSetCollect(Arrays.stream(rc.senseNearbyRobots()).filter(robotInfo -> robotInfo.getTeam() == rc.getTeam()));
	}
	
	public static Optional<Integer> findFirstAdjacentAssignmentFlag(RobotController rc) throws GameActionException {
		final int maxAdjacentDistanceSquared = 2;
		
		BiPredicate<RobotInfo, Integer> isAssignmentFlag = new BiPredicate<RobotInfo, Integer>() {
			Set<Class<? extends Flag.IFlag>> assignmentClasses = new HashSet<>(Arrays.asList(AttackAssignmentFlag.class, PatrolAssignmentFlag.class));
			@Override
			public boolean test(RobotInfo robotInfo, Integer rawFlag) {
				Flag.IFlag flag = Flag.decode(rawFlag);
				return assignmentClasses.contains(flag.getClass());
			}
			
		};
		
		Optional<SimpleImmutableEntry<RobotInfo, Integer>> entryOpt = HandlerCommon.findFirstMatchingTeamFlag(
				rc,
				rc.senseNearbyRobots(maxAdjacentDistanceSquared, rc.getTeam()),  // TODO(theimer): constant!
				isAssignmentFlag
		);
		
		if (entryOpt.isPresent()) {
			return Optional.of(entryOpt.get().getValue());
		} else {
			return Optional.empty();
		}
	}
}
