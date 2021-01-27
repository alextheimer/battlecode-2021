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
import java.util.stream.Stream;

import battlecode.common.*;
import player.handlers.common.HandlerCommon;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.util.UtilFlag.OpCode;
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
	
	public static MapLocation offsetToMapLocation(IntVec2D offset, MapLocation validMapLocation) {
		assert UtilMath.isPow2(UtilBattlecode.MAX_WORLD_WIDTH);
		assert (offset.x >= 0) && (offset.x < 2 * UtilBattlecode.MAX_WORLD_WIDTH) : "" + offset.x;
		assert (offset.y >= 0) && (offset.y < 2 * UtilBattlecode.MAX_WORLD_WIDTH) : "" + offset.y;
		
		final int modVal = 2 * UtilBattlecode.MAX_WORLD_WIDTH;
		final int mask = modVal - 1;
		int xValidOffset = validMapLocation.x & mask;
		int yValidOffset = validMapLocation.y & mask;
		int xDiff = UtilMath.diffMod(offset.x, xValidOffset, modVal);
		int yDiff = UtilMath.diffMod(offset.y, yValidOffset, modVal);
		return new MapLocation(validMapLocation.x + xDiff, validMapLocation.y + yDiff);
	}
	
	public static IntVec2D mapLocationToOffset(MapLocation mapLocation) {
		final int modVal = 2 * UtilBattlecode.MAX_WORLD_WIDTH;
		final int mask = modVal - 1;
		return new IntVec2D(mapLocation.x & mask, mapLocation.y & mask);
	}
}
