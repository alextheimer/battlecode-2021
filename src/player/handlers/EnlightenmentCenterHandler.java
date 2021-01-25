package player.handlers;

import battlecode.common.*;
import player.util.Util.PeekableIteratorWrapper;
import player.util.UtilMath;
import player.util.UtilMath.DoubleVec2D;
import player.util.UtilMath.IntVec2D;
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
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import static player.handlers.HandlerCommon.*;

class Target implements Comparable<Target> {
	
	RobotType robotType;
	MapLocation mapLoc;
	
	public Target(RobotType robotType, MapLocation mapLoc) {
		this.robotType = robotType;
		this.mapLoc = mapLoc;
	}
	
	@Override
	public int compareTo(Target other) {
		return 0;
	}

}

class Blueprint {
	public RobotType robotType;
	public AssignmentType assignmentType;
	public Blueprint(RobotType robotType, AssignmentType assignmentType) {
		this.robotType = robotType;
		this.assignmentType = assignmentType;
	}
}

public class EnlightenmentCenterHandler implements IRobotHandler {
	
	private static final int FLAG_COOLDOWN_START = 1;
	private static final Random rand = new Random();
    
	private int buildSequenceIndex = 0;
	private int flagCooldown = 0;
	private Queue<Target> targetQueue = new PriorityQueue<>();
	
    private boolean attemptBuild(RobotController rc, Blueprint blueprint, AssignmentFlag flag) throws GameActionException {
    	final int influence = 50;
    	boolean buildSuccess = false;
    	for (Direction dir : HandlerCommon.directions) {
    		if (rc.canBuildRobot(blueprint.robotType, dir, influence)) {
    			rc.buildRobot(blueprint.robotType, dir, influence);
    			rc.setFlag(flag.encode());
    			this.flagCooldown = FLAG_COOLDOWN_START;
    			buildSuccess = true;
    			break;
    		}
    	}
    	return buildSuccess;
    }
    
    private Blueprint makeBlueprint(Target target) {
    	return new Blueprint(RobotType.POLITICIAN, AssignmentType.PATROL);
    }
    
    private boolean targetFilter(Target target) {
    	return target.robotType == RobotType.ENLIGHTENMENT_CENTER;
    }
    
	@Override
	public IRobotHandler handle(RobotController rc) throws GameActionException {

		if (this.flagCooldown > 0) {
			// just built something; still need to keep its assignment flag up
			this.flagCooldown--;
			return this;
		}
		
		// store this; use below
		RobotInfo[] sensedRobots = rc.senseNearbyRobots();
		
		// check if we need to call for help
		Optional<RobotInfo> nearestNonTeam = HandlerCommon.senseNearestNonTeam(rc, sensedRobots);
		if (nearestNonTeam.isPresent()) {
			// TODO(theimer): call for help!
		}
		
		// look for EC's in need of help; build and deploy if necessary
		// TODO(theimer): !!!
		
		// look for enemy callouts; screen; store targets
		Map<RobotInfo, Integer> robotInfoFlagMap = HandlerCommon.findAllMatchingTeamFlags(rc, sensedRobots, (robotInfo, rawFlag) -> (Flag.getOpCode(rawFlag) == OpCode.ENEMY_SIGHTED));
		for (Map.Entry<RobotInfo, Integer> entry : robotInfoFlagMap.entrySet()) {
			int rawFlag = entry.getValue();
			EnemySightedFlag flag = EnemySightedFlag.decode(rawFlag);
			RobotType robotType = flag.getRobotType();
			IntVec2D flagOffset = flag.getCoord();
			MapLocation mapLoc = HandlerCommon.offsetToMapLocation(flagOffset, rc.getLocation());
			Target target = new Target(robotType, mapLoc);
			if (this.targetFilter(target)) {
				this.targetQueue.add(target);
			}
		}
		
		// select Target / Blueprint, build / deplay
		if (this.targetQueue.size() > 0) {
			Target target = this.targetQueue.peek();
			Blueprint blueprint = this.makeBlueprint(target);
			MapLocation currMapLoc = rc.getLocation();
			MapLocation targetMapLoc = target.mapLoc;
			DoubleVec2D vec = new DoubleVec2D(targetMapLoc.x - currMapLoc.x, targetMapLoc.y - currMapLoc.y);
			int degrees = (int)UtilMath.vecToDegrees(vec);
			Flag.AssignmentFlag flag = new Flag.AssignmentFlag(blueprint.assignmentType, degrees);
			if (this.attemptBuild(rc, blueprint, flag) ) {
				this.targetQueue.remove();
			}
		} else {
			Blueprint blueprint = new Blueprint(RobotType.POLITICIAN, AssignmentType.PATROL);
			Flag.AssignmentFlag flag = new Flag.AssignmentFlag(blueprint.assignmentType, rand.nextInt(UtilMath.MAX_DEGREES));
			this.attemptBuild(rc, blueprint, flag);
		}
        
        return this;
	}
}
