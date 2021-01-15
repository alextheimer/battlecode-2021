package player.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import battlecode.common.*;
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
    public static final int NULL_ROBOT_ID = -1;
    
    // TODO(theimer) default state fields
    public static class SquadOrders {
    	public SquadType squadType;
    	public int leaderID;
    	public Line2D pathLine;
    	public DoubleVec2D pathVec;
    	public List<Integer> squadIDs;
    	public SquadOrders() {
    		squadType = null;
    		pathVec = null;
    		pathLine = null;
    		leaderID = NULL_ROBOT_ID;
    		squadIDs = new ArrayList<>();
    	}
    };
    
    public static class RobotState {
    	public RobotRole role;
    	public SquadOrders orders;
    	public int targetID;
    	public RobotState(RobotRole role, SquadOrders orders) {
    		this.role = role;
    		this.orders = orders;
    		this.targetID = NULL_ROBOT_ID;
    	}
    };
    
    public interface IRobotRoleHandler {
    	public IRobotRoleHandler handle(RobotController rc, RobotState state) throws GameActionException;
    }
    
    public interface IRobotTypeHandler {
    	public IRobotTypeHandler handle(RobotController rc, RobotState state) throws GameActionException;
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
