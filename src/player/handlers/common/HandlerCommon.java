package player.handlers.common;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import player.util.math.DoubleVec2D;
import player.util.math.IntVec2D;
import player.util.math.UtilMath;
import player.util.math.UtilMath.*;

/**
 * Contains functions common among handlers.
 * 
 * Note: all "attempt" functions check that a game action is possible before performing the action.
 *     If it's possible, it executes; otherwise, it does not. The functions returns true
 *     iff the action executes.
 */
public class HandlerCommon {
	
	/**
	 * Attempts to move the robot into the adjacent space as specified by the argument Direction.
	 * 
	 * @param rc the RobotController for the current round.
	 * @return true iff the move completes successfully; else false.
	 */
	public static boolean attemptMove(RobotController rc, Direction dir) {
		UtilBattlecode.log("Attempting to move Direction: " + dir);
		boolean moveSuccessful;
		if (rc.canMove(dir)) {
			try {
				rc.move(dir);				
			} catch (GameActionException e) {
				// rc::canMove is a precondition of the try-catch.
				throw new UtilBattlecode.IllegalGameActionException(e);
			}
			moveSuccessful = true;
		} else {
			moveSuccessful = false;
		}
		UtilBattlecode.log("Move success: " + moveSuccessful);
		return moveSuccessful;
	}
	
	/**
	 * Returns a Stream of RobotInfos on the specified Team.
	 * 
	 * @param sensedRobots RobotInfos sensed by the RobotController for the current round.
	 */
	public static Stream<RobotInfo> getRobotsOnTeamStream(Team team, RobotInfo[] sensedRobots) {
		return Arrays.stream(sensedRobots)
				.filter(robotInfo -> robotInfo.getTeam() == team);
	}
	
	/**
	 * Returns a Stream of RobotInfos ***NOT*** on the specified Team.
	 * 
	 * @param sensedRobots RobotInfos sensed by the RobotController for the current round.
	 */
	public static Stream<RobotInfo> getRobotsNotOnTeamStream(Team notThisTeam, RobotInfo[] sensedRobots) {
		return Arrays.stream(sensedRobots)
				.filter(robotInfo -> robotInfo.getTeam() != notThisTeam);
	}
	
	/**
	 * Returns a stream of RobotInfo/IFlag pairs such that each RobotInfo
	 * robot is on the same Team as the RobotController robot.
	 * 
	 * @param rc the RobotController for the current round.
	 * @param sensedRobots RobotInfos sensed by the RobotController for the current round.
	 */
	public static Stream<SimpleImmutableEntry<RobotInfo, IFlag>> getMatchingTeamFlagsStream(RobotController rc, 
			                                                                                RobotInfo[] sensedRobots) {
		return getRobotsOnTeamStream(rc.getTeam(), sensedRobots)
		    // map to RobotInfo/IFlag pairs
		    .map(new Function<RobotInfo, SimpleImmutableEntry<RobotInfo, IFlag>>() {

				@Override
				public SimpleImmutableEntry<RobotInfo, IFlag> apply(RobotInfo robotInfo) {
					int rawFlag;
					try {
						rawFlag = rc.getFlag(robotInfo.getID());
					} catch (GameActionException e) {
						// This should never happen-- sensedRobots must be sensable at the current round.
						throw new UtilBattlecode.IllegalGameActionException(e);
					}
					return new SimpleImmutableEntry<RobotInfo, IFlag>(robotInfo, Flag.decode(rawFlag));
				}
			});
	}
	
	
	/**
	 * Returns the robot nearest to the RobotController robot that is ***NOT*** on the same team.
	 * 
	 * @param rc the RobotController for the current round.
	 * @param sensedRobots RobotInfos sensed by the RobotController for the current round.
	 * @return an occupied Optional iff sensedRobots contains at least one non-team RobotInfo;
	 *     else returns an empty Optional.
	 */
	public static Optional<RobotInfo> getNearestNonTeamRobot(RobotController rc, RobotInfo[] sensedRobots) {
		Iterator<RobotInfo> nonTeamRobotIterator = getRobotsNotOnTeamStream(rc.getTeam(), sensedRobots).iterator();
		Optional<RobotInfo> result;
		if (nonTeamRobotIterator.hasNext()) {
			Function<RobotInfo, Double> costFunc = robotInfo -> (double)rc.getLocation().distanceSquaredTo(robotInfo.getLocation());
			result = Optional.of(UtilGeneral.findLeastCostLinear(nonTeamRobotIterator, costFunc));
		} else {
			result = Optional.empty();
		}
		return result;
	}
	
	
	/**
	 * Returns some robot/flag pair such that:
	 *     (1) the robot is immediately adjacent to the RobotController robot, and
	 *     (2) the robot is on the same Team as the RobotController robot, and
	 *     (3) the flag is an "assignment" flag.  // TODO(theimer): make this definition more clear/concrete
	 * 
	 * @param rc the RobotController for the current round.
	 * @param sensedRobots RobotInfos sensed by the RobotController for the current round.
	 * @return an occupied Optional iff a RobotInfo in `sensedRobots` has posted a flag, and the
	 *     RobotInfo/IFlag pair meets the above three criteria.
	 */
	public static Optional<SimpleImmutableEntry<RobotInfo, IFlag>> getAnyAdjacentAssignmentFlag(RobotController rc,
			                                                                                    RobotInfo[] sensedRobots) {
		// max distance squared between two adjacent MapLocations
		final int maxAdjacentDistanceSquared = 2;
		
		BiPredicate<RobotInfo, IFlag> predicate = new BiPredicate<RobotInfo, IFlag>() {

			@Override
			public boolean test(RobotInfo robotInfo, IFlag flag) {
				return
						// adjacent to the RobotController robot
						(rc.getLocation().distanceSquaredTo(robotInfo.getLocation()) <= maxAdjacentDistanceSquared) &&
						// is an assignment flag
						(flag instanceof PatrolAssignmentFlag || flag instanceof AttackAssignmentFlag);
			}
			
		};
		
		return getMatchingTeamFlagsStream(rc, sensedRobots)
				.filter(entry -> predicate.test(entry.getKey(), entry.getValue()))
				.findAny();
	}
	
	
	/**
	 * Returns a Stream of all locations that the RobotController can move into such that each:
	 *     (1) is adjacent to the robot's current location, and
	 *     (2) is on the map.
	 * 
	 * @param rc the RobotController for the current round.
	 */
	public static Stream<MapLocation> makeValidAdjacentMapLocStream(RobotController rc) {
		MapLocation currentMapLoc = rc.getLocation();
		
		Stream<MapLocation> adjacentMapLocStream = UtilBattlecode.makeAllAdjacentMapLocStream(rc.getLocation());
		
		// Note: GameActionException will not be thrown; adjacent MapLocations are always in sensor range.
		Predicate<MapLocation> onTheMapPredicate = UtilBattlecode.<MapLocation>silenceGameActionPredicate(mapLoc -> rc.onTheMap(mapLoc));
		
		return adjacentMapLocStream.filter(onTheMapPredicate);
	}
}
