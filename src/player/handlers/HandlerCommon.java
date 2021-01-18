package player.handlers;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import battlecode.common.*;
import player.handlers.roletype.SquadState;
import util.Flag;
import util.Flag.OpCode;
import util.Util;
import util.UtilMath.*;


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
    
    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}
