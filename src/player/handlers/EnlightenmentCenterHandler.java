package player.handlers;

import battlecode.common.*;
import player.util.Util.PeekableIteratorWrapper;
import player.util.UtilMath;
import player.util.UtilMath.DoubleVec2D;
import player.util.UtilMath.IntVec2D;
import player.util.Flag;
import player.util.Flag.*;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

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
	private static final int DEGREES_DELTA = 5;
	private static final int DEGREES_START = 0;
	
	private int buildSequenceIndex = 0;
	private int flagCooldown = 0;
	private Queue<Target> targetQueue = new PriorityQueue<>();
	private int nextDegrees = DEGREES_START;
	private Set<Integer> idSet = new HashSet<>();
	
    private boolean attemptBuild(RobotController rc, Blueprint blueprint, IFlag assignmentFlag) throws GameActionException {
    	final int influence = 50;
    	boolean buildSuccess = false;
    	for (Direction dir : HandlerCommon.directions) {
    		if (rc.canBuildRobot(blueprint.robotType, dir, influence)) {
    			rc.buildRobot(blueprint.robotType, dir, influence);
    			rc.setFlag(assignmentFlag.encode());
    			this.flagCooldown = FLAG_COOLDOWN_START;
    			buildSuccess = true;
    			break;
    		}
    	}
    	System.out.println("Build attempted; success: " + buildSuccess);
    	return buildSuccess;
    }
    
    private Blueprint makeBlueprint(Target target) {
    	return new Blueprint(RobotType.POLITICIAN, AssignmentType.ATTACK_TARGET);
    }
    
    private boolean targetFilter(Target target, RobotController rc) {
    	return (target.robotType == RobotType.ENLIGHTENMENT_CENTER) && !rc.getLocation().equals(target.mapLoc);
    }
    
    private int incrementDegrees() {
    	int nextDegrees = this.nextDegrees;
    	this.nextDegrees += 90;
    	if (this.nextDegrees >= UtilMath.MAX_DEGREES) {
    		this.nextDegrees -= UtilMath.MAX_DEGREES;
    		this.nextDegrees += DEGREES_DELTA;
    	}
    	return nextDegrees;
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
		
		// update our known ID's
		Set<RobotInfo> teamSet = HandlerCommon.senseAllTeam(rc);
		for (RobotInfo robotInfo : teamSet) {
			if (!this.idSet.contains(robotInfo.getID())) {
				this.idSet.add(robotInfo.getID());
			}
		}
		
		// check if we need to call for help
		Optional<RobotInfo> nearestNonTeam = HandlerCommon.senseNearestNonTeam(rc, sensedRobots);
		if (nearestNonTeam.isPresent()) {
			// TODO(theimer): call for help!
		}
		
		// look for EC's in need of help; build and deploy if necessary
		// TODO(theimer): !!!
		
		// look for enemy callouts; screen; store targets
		Iterator<Integer> idIterator = this.idSet.iterator();
		while (idIterator.hasNext()) {
			int id = idIterator.next();
			if (rc.canGetFlag(id)) {
				int rawFlag = rc.getFlag(id);
				if (Flag.getOpCode(rawFlag) == OpCode.ENEMY_SIGHTED) {
					EnemySightedFlag flag = EnemySightedFlag.decode(rawFlag);
					RobotType robotType = flag.getRobotType();
					IntVec2D flagOffset = flag.getCoord();
					MapLocation mapLoc = HandlerCommon.offsetToMapLocation(flagOffset, rc.getLocation());
					Target target = new Target(robotType, mapLoc);
					if (this.targetFilter(target, rc)) {
						this.targetQueue.add(target);
						System.out.println("Added target @ " + target.mapLoc);
					}					
				}
			} else {
				idIterator.remove();
			}
		}
		
		// select Target / Blueprint, build / deplay
		if (this.targetQueue.size() > 0) {
			System.out.println("HAS TARGET");
			Target target = this.targetQueue.peek();
			Blueprint blueprint = this.makeBlueprint(target);
			MapLocation currMapLoc = rc.getLocation();
			MapLocation targetMapLoc = target.mapLoc;
			DoubleVec2D vec = new DoubleVec2D(targetMapLoc.x - currMapLoc.x, targetMapLoc.y - currMapLoc.y);
			int degrees = (int)UtilMath.vecToDegrees(vec);
			Flag.AttackAssignmentFlag flag = new Flag.AttackAssignmentFlag(targetMapLoc.x, targetMapLoc.y);
			if (this.attemptBuild(rc, blueprint, flag) ) {
				this.targetQueue.remove();  // TODO(theimer): !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			}
		} else {
			Blueprint blueprint = new Blueprint(RobotType.POLITICIAN, AssignmentType.PATROL);
			Flag.PatrolAssignmentFlag flag = new Flag.PatrolAssignmentFlag(this.nextDegrees);
			if (this.attemptBuild(rc, blueprint, flag)) {
				this.incrementDegrees();
			}
			
		}
        
        return this;
	}
}
