package player.handlers;

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
import player.handlers.HandlerCommon;
import player.util.Flag;
import player.util.Flag.OpCode;
import player.util.Util;
import player.util.UtilMath;
import player.util.UtilMath.*;


public class HandlerCommon {
	
	public static final int MAX_DIST_SQUARED_ADJACENT = 2;
	public static final int MAX_WORLD_WIDTH = 64;
	
    public static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };
    
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    
    public static enum AssignmentType {PATROL, OCCUPY, UNASSIGNED};
    
    public interface IRobotHandler {
    	public IRobotHandler handle(RobotController rc) throws GameActionException;
    }
    
    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    public static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }
    
    public static Iterator<MapLocation> getAdjacentIterator(MapLocation mapLoc) {
    	return new Iterator<MapLocation>() {
    		
    		private int dirIndex = 0;

			@Override
			public boolean hasNext() {
				return (dirIndex < (directions.length));
			}

			@Override
			public MapLocation next() {
				Direction dir = directions[dirIndex];
				++dirIndex;
				return mapLoc.add(dir);
			}
    		
    	};
    }
    
	@FunctionalInterface
	public static interface GameActionPredicate<T> {
		public boolean test(T t) throws GameActionException;
	}
    
	@FunctionalInterface
	public static interface GameActionFunction<T, R> {
		public R apply(T t) throws GameActionException;
	}

	public static <T> Predicate<T> wrapGameActionPredicate(GameActionPredicate<T> pred) {
		return new Predicate<T>() {
			@Override
			public boolean test(T t) {
				try {
					return pred.test(t);
				} catch (GameActionException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					throw new RuntimeException("Use of this function implies this should never happen!");
				}
			}
			
		};
	}
	
	public static boolean attemptMove(RobotController rc, Direction dir) throws GameActionException {
		System.out.println("Want to move: " + dir);
		if (rc.canMove(dir)) {
			rc.move(dir);
			System.out.println("Move successful!");
			return true;
		} else {
			System.out.println("Move failed!");
			return false;
		}
	}
    
	public static Map<RobotInfo, Integer> findAllMatchingFlags(RobotController rc, RobotInfo[] nearbyRobots,
	                                   BiPredicate<RobotInfo, Integer> predicate) throws GameActionException  {
		Map<RobotInfo, Integer> robotFlagMap = new HashMap<>();
		for (RobotInfo robotInfo : nearbyRobots) {
			int rawFlag = rc.getFlag(robotInfo.getID());
			if (predicate.test(robotInfo, rawFlag)) {
				robotFlagMap.put(robotInfo, rawFlag);
			}
		}
		return robotFlagMap;
	}
	
	public static Optional<SimpleImmutableEntry<RobotInfo, Integer>> findFirstMatchingFlag(RobotController rc, RobotInfo[] nearbyRobots,
                                       BiPredicate<RobotInfo, Integer> predicate) throws GameActionException  {
		for (RobotInfo robotInfo : nearbyRobots) {
			int rawFlag = rc.getFlag(robotInfo.getID());
			if (predicate.test(robotInfo, rawFlag)) {
				return Optional.of(new SimpleImmutableEntry<RobotInfo, Integer>(robotInfo, rawFlag));
			}
		}
		return Optional.empty();
	}

	public static Optional<RobotInfo> senseNearestNonTeam(RobotController rc, RobotInfo[] nearbyRobots) {
		Iterator<RobotInfo> otherTeamIterator = Arrays.stream(nearbyRobots).filter(robotInfo -> robotInfo.getTeam() != rc.getTeam()).iterator();
		if (otherTeamIterator.hasNext()) {
			Function<RobotInfo, Double> costFunc = robotInfo -> (double)robotInfo.getLocation().distanceSquaredTo(rc.getLocation());
			return Optional.of(Util.findLeastCostLinear(otherTeamIterator, costFunc));
		} else {
			return Optional.empty();
		}
	}
	
	public static Set<RobotInfo> senseAllNonTeam(RobotController rc) {
		return Util.legalSetCollect(Arrays.stream(rc.senseNearbyRobots()).filter(robotInfo -> robotInfo.getTeam() != rc.getTeam()));
	}
	
	public static MapLocation offsetToMapLocation(IntVec2D offset, MapLocation validMapLocation) {
		assert UtilMath.isPow2(MAX_WORLD_WIDTH);
		assert offset.x >= 0 && offset.x < MAX_WORLD_WIDTH;
		assert offset.y >= 0 && offset.y < MAX_WORLD_WIDTH;
		
		final int modVal = 2 * MAX_WORLD_WIDTH;
		final int mask = modVal - 1;
		int xValidOffset = validMapLocation.x & mask;
		int yValidOffset = validMapLocation.y & mask;
		int xDiff = UtilMath.diffMod(offset.x, xValidOffset, modVal);
		int yDiff = UtilMath.diffMod(offset.y, yValidOffset, modVal);
		return new MapLocation(validMapLocation.x + xDiff, validMapLocation.y + yDiff);
	}
}
