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
import player.util.UtilMath.*;


public class HandlerCommon {
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
    
    public static enum RobotRole {UNASSIGNED, LEADER, FOLLOWER, NONE};
    public static enum SquadType {PATROL, OCCUPY, UNASSIGNED, NONE};
    
    public interface IRobotRoleHandler {
    	public IRobotRoleHandler handle(RobotController rc) throws GameActionException;
    }
    
    public interface IRobotTypeHandler {
    	public IRobotTypeHandler handle(RobotController rc) throws GameActionException;
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
    
	// TODO(theimer): break this up / accept ranking heuristic
    public static Optional<MapLocation> getAdjacentCloserTraversableMapLocation(MapLocation start, MapLocation goal, RobotController rc) {
    	int distSquaredToGoal = start.distanceSquaredTo(goal);
		Predicate<MapLocation> pred = HandlerCommon.<MapLocation>wrapGameActionPredicate(
			mapLoc -> (
			    (mapLoc.distanceSquaredTo(goal) < distSquaredToGoal) &&
			    !rc.isLocationOccupied(mapLoc) && rc.onTheMap(mapLoc)
			)
		);
    	Stream<MapLocation> mapLocStream = Util.streamifyIterator(getAdjacentIterator(start)).filter(pred);
    	return mapLocStream.findAny();
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

	public static Direction directionToGoal(MapLocation startCoord, MapLocation goalCoord) {
		// TODO(theimer): literally anything better than this
		if (goalCoord.x > startCoord.x) {
			if (goalCoord.y > startCoord.y) {
				return Direction.NORTHEAST;
			} else {
				return Direction.SOUTHEAST;
			}
		} else {
			if (goalCoord.y > startCoord.y) {
				return Direction.NORTHWEST;
			} else {
				return Direction.SOUTHWEST;
			}			
		}
	}

	public static void battlecodeAssert(boolean resignIfFalse, String message, RobotController rc) {
		if (!resignIfFalse) {
			System.out.println("ASSESRTION FAIL: " + message);
			rc.resign();
		}
	}

	public static void battlecodeThrow(String message, RobotController rc) {
		System.out.println("EXCEPTION THROWN: " + message);
		rc.resign();
	}

	public static Optional<RobotInfo> findNearestEnemy(RobotController rc, RobotInfo[] nearbyRobots) {
		Iterator<RobotInfo> enemyIterator = Arrays.stream(nearbyRobots).filter(robotInfo -> robotInfo.getTeam() == rc.getTeam().opponent()).iterator();
		if (enemyIterator.hasNext()) {
			Function<RobotInfo, Double> costFunc = robotInfo -> (double)robotInfo.getLocation().distanceSquaredTo(rc.getLocation());
			return Optional.of(Util.findLeastCostLinear(enemyIterator, costFunc));
		} else {
			return Optional.empty();
		}
	}
}
