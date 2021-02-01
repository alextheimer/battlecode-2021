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
	
	public static Stream<RobotInfo> getRobotsOnTeamStream(Team team, RobotInfo[] sensedRobots) {
		return Arrays.stream(sensedRobots)
				.filter(robotInfo -> robotInfo.getTeam() == team);
	}
	
	public static Stream<RobotInfo> getRobotsNotOnTeamStream(Team notThisTeam, RobotInfo[] sensedRobots) {
		return Arrays.stream(sensedRobots)
				.filter(robotInfo -> robotInfo.getTeam() != notThisTeam);
	}
	
	public static Stream<SimpleImmutableEntry<RobotInfo, IFlag>> getMatchingTeamFlagsStream(RobotController rc,
			RobotInfo[] sensedRobots, BiPredicate<RobotInfo, IFlag> predicate) throws GameActionException  {
		return getRobotsOnTeamStream(rc.getTeam(), sensedRobots)
		    // map to RobotInfo/IFlag pairs
		    .map(new Function<RobotInfo, SimpleImmutableEntry<RobotInfo, IFlag>>() {

				@Override
				public SimpleImmutableEntry<RobotInfo, IFlag> apply(RobotInfo robotInfo) {
					int rawFlag;
					try {
						rawFlag = rc.getFlag(robotInfo.getID());
					} catch (GameActionException e) {
						// This should never happen-- sensedRobots must be sensable.
						throw new UtilBattlecode.IllegalGameActionException(e);
					}
					return new SimpleImmutableEntry<RobotInfo, IFlag>(robotInfo, Flag.decode(rawFlag));
				}
		    	
		    })
		    // filter out the pairs we don't want
		    .filter(entry -> predicate.test(entry.getKey(), entry.getValue()));
	}
	
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
	
	public static Optional<SimpleImmutableEntry<RobotInfo, IFlag>> getAnyAdjacentAssignmentFlag(RobotController rc,
			RobotInfo[] sensedRobots) throws GameActionException {
		
		final int maxAdjacentDistanceSquared = 3;
		
		BiPredicate<RobotInfo, IFlag> predicate = new BiPredicate<RobotInfo, IFlag>() {

			@Override
			public boolean test(RobotInfo robotInfo, IFlag flag) {
				return
						// adjacent to the RobotController robot
						(rc.getLocation().distanceSquaredTo(robotInfo.getLocation()) < maxAdjacentDistanceSquared) &&
						// is an assignment flag
						(flag instanceof PatrolAssignmentFlag || flag instanceof AttackAssignmentFlag);
			}
			
		};
		
		return getMatchingTeamFlagsStream(rc, sensedRobots, predicate).findAny();
	}
}
