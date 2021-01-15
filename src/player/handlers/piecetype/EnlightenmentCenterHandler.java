package player.handlers.piecetype;

import battlecode.common.*;
import util.Util.PeekableIteratorWrapper;
import util.Flag;
import util.Flag.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static player.handlers.HandlerCommon.*;

public class EnlightenmentCenterHandler implements IRobotTypeHandler {
	
	private static final int FLAG_COOLDOWN_START = 2;  // 1 round for all bots to see SquadAssignFlag
	                                                   // 1 round for all bots to see LeaderClaimFlag
	private static Map<SquadType, List<RobotType>> squadSpecMap = new HashMap<>();
	static {
		// TODO(theimer): first is the leader?
		squadSpecMap.put(SquadType.PATROL, Arrays.asList(RobotType.POLITICIAN, RobotType.POLITICIAN));
	}
    
	private PeekableIteratorWrapper<RobotType> robotBuildIterator;
	private Direction nextBuildDir;
	private int flagCooldown;
	
	public EnlightenmentCenterHandler() {
		robotBuildIterator = new PeekableIteratorWrapper<>(Collections.emptyIterator());
		nextBuildDir = Direction.NORTH;
		flagCooldown = 0;
	}
	
	// TODO(theimer): move to util?
	private static Direction rotateDirectionClockwise(Direction dir) {
		// TODO(theimer): use a map
		switch (dir) {
			case NORTH: return Direction.NORTHEAST;
			case NORTHEAST: return Direction.EAST;
			case EAST: return Direction.SOUTHEAST;
			case SOUTHEAST: return Direction.SOUTH;
			case SOUTH: return Direction.SOUTHWEST;
			case SOUTHWEST: return Direction.WEST;
			case WEST: return Direction.NORTHWEST;
			case NORTHWEST: return Direction.NORTHEAST;
			case CENTER: throw new IllegalArgumentException();
			default: throw new IllegalArgumentException();
		}
	}
	
    private static SquadType chooseSquadType() {
    	return SquadType.PATROL;
    }
	
    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    private static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }
	
    private Optional<Direction> buildableDirection(RobotController rc) {
    	// TODO(theimer): constants?
    	Direction dir = this.nextBuildDir;
    	for (int i = 0; i < 8; ++i) {
    		// TODO(theimer): this is the cheapest way to do this?
    		if (rc.canBuildRobot(RobotType.POLITICIAN, dir, 1)) {
    			return Optional.of(dir);
    		}
    		dir = rotateDirectionClockwise(dir);
    	}
    	return Optional.empty();
    }
    
    private boolean attemptBuildNext(RobotController rc) throws GameActionException {
    	int influence = 50;
        RobotType typeToBuild = this.robotBuildIterator.peek();
        Optional<Direction> dirToBuildOptional = buildableDirection(rc);
        if (dirToBuildOptional.isPresent()) {
        	// we can build in some direction
        	Direction dirToBuild = dirToBuildOptional.get();
        	if (rc.canBuildRobot(typeToBuild, dirToBuild, influence)) {
        		// we can successfully build the robot
        		this.robotBuildIterator.next();
        		this.nextBuildDir = rotateDirectionClockwise(dirToBuild);
        		rc.buildRobot(typeToBuild, dirToBuild, influence);
        		return true;
        	}
        }
        return false;
    }
    
	@Override
	public IRobotTypeHandler handle(RobotController rc) throws GameActionException {
		
		if (this.flagCooldown > 0) {
			this.flagCooldown--;
			return this;
		}
		
        if (!this.robotBuildIterator.hasNext()) {
        	// queue a squad for construction
        	SquadType squadToQueue = chooseSquadType();
        	this.robotBuildIterator = new PeekableIteratorWrapper<>(squadSpecMap.get(squadToQueue).iterator());
        	rc.setFlag(Flag.EMPTY_FLAG);
        }
        boolean builtRobot = false;
        if (this.robotBuildIterator.hasNext()) {
        	// attempt to build the next robot of the squad
        	builtRobot = this.attemptBuildNext(rc);        	
        }
        
        if (builtRobot && !this.robotBuildIterator.hasNext()) {
        	// built the last robot of a squad
        	SquadAssignFlag flag = new SquadAssignFlag(SquadType.PATROL, 90);
        	rc.setFlag(flag.encode());
        	this.flagCooldown = FLAG_COOLDOWN_START;
        }
        
        return this;
	}
}
