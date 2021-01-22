package player.handlers;

import battlecode.common.*;
import player.util.Util.PeekableIteratorWrapper;
import player.util.UtilMath;
import player.util.Flag;
import player.util.Flag.*;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static player.handlers.HandlerCommon.*;

public class EnlightenmentCenterHandler implements IRobotHandler {
	
	private static final int FLAG_COOLDOWN_START = 1;
	private static final Random rand = new Random();
	
	private static class Blueprint {
		public RobotType robotType;
		public AssignmentType assignment;
		public Blueprint(RobotType robotType, AssignmentType assignment) {
			this.robotType = robotType;
			this.assignment = assignment;
		}
	}
	
	private static final List<Blueprint> buildTypeSequence = Arrays.asList(
			new Blueprint(RobotType.POLITICIAN, AssignmentType.PATROL)
	);
    
	private int buildSequenceIndex = 0;
	private int flagCooldown = 0;
	
    private boolean attemptBuild(RobotController rc, RobotType robotType) throws GameActionException {
    	final int influence = 50;
    	boolean buildSuccess = false;
    	for (Direction dir : HandlerCommon.directions) {
    		if (rc.canBuildRobot(robotType, dir, influence)) {
    			rc.buildRobot(robotType, dir, influence);
    			buildSuccess = true;
    			break;
    		}
    	}
    	return buildSuccess;
    }
    
    private boolean attemptBuildNext(RobotController rc) throws GameActionException {
    	RobotType nextRobotType = EnlightenmentCenterHandler.buildTypeSequence.get(this.buildSequenceIndex).robotType;
    	boolean buildSuccess = false;
    	if (this.attemptBuild(rc, nextRobotType)) {
    		buildSuccess = true;
    		this.buildSequenceIndex = (this.buildSequenceIndex + 1) % EnlightenmentCenterHandler.buildTypeSequence.size();
    	}
    	return buildSuccess;
    }
    
	@Override
	public IRobotHandler handle(RobotController rc) throws GameActionException {
		Optional<RobotInfo> nearestEnemyOpt = HandlerCommon.senseNearestNonTeam(rc, rc.senseNearbyRobots());
		
		
//		if (h)
		
		if (this.flagCooldown > 0) {
			this.flagCooldown--;
			return this;
		}
		
		if (rc.canSetFlag(Flag.EMPTY_FLAG)) {
			if (attemptBuildNext(rc)) {
				Flag.AssignmentFlag flag = new Flag.AssignmentFlag(AssignmentType.PATROL, rand.nextInt(UtilMath.MAX_DEGREES));
				rc.setFlag(flag.encode());
				this.flagCooldown = FLAG_COOLDOWN_START;
			}
		}
        
        return this;
	}
}
