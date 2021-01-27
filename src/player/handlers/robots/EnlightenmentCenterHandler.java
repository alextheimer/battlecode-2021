package player.handlers.robots;

import battlecode.common.*;
import player.RobotPlayer;
import player.RobotPlayer.IRobotHandler;
import player.handlers.common.HandlerCommon;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.*;
import player.util.math.IntVec2D;
import player.util.math.UtilMath;

import java.util.AbstractMap.SimpleImmutableEntry;

import static player.handlers.common.HandlerCommon.*;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

class Target implements Comparable<Target> {
	
	RobotType robotType;
	MapLocation mapLoc;
	
	public Target(RobotType robotType, MapLocation mapLoc) {
		this.robotType = robotType;
		this.mapLoc = mapLoc;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(robotType, mapLoc);
	}
	
	public boolean sameValue(Target other) {
		return (this.robotType == other.robotType) && (this.mapLoc.equals(other.mapLoc));
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof Target) && this.sameValue((Target)other);
	}
	
	@Override
	public int compareTo(Target other) {
		return 0;
	}

}

class Blueprint {
	public RobotType robotType;
	public EnlightenmentCenterHandler.Assignment assignmentType;
	public Blueprint(RobotType robotType, EnlightenmentCenterHandler.Assignment assignmentType) {
		this.robotType = robotType;
		this.assignmentType = assignmentType;
	}
}

public class EnlightenmentCenterHandler implements RobotPlayer.IRobotHandler {
	
	/**
	 * Each unit is given an Assignment 
	 */
	public static enum Assignment { PATROL, ATTACK_TARGET, UNASSIGNED }
	
	private static final int FLAG_COOLDOWN_START = 1;
	private static final int DEGREES_DELTA = 5;
	private static final int DEGREES_START = 0;
	
	private int flagCooldown = 0;
	private Queue<Target> targetQueue = new PriorityQueue<>();
	private Set<Target> addedTargets = new HashSet<>();
	private int nextDegrees = DEGREES_START;
	private Set<Integer> idSet = new HashSet<>();
	private int buildNum = 0;
	
    private boolean attemptBuild(RobotController rc, Blueprint blueprint, IFlag assignmentFlag) throws GameActionException {
    	final int influence = rc.getInfluence() - 1;
    	boolean buildSuccess = false;
    	for (Direction dir : UtilBattlecode.OFF_CENTER_DIRECTIONS) {
    		if (rc.canBuildRobot(blueprint.robotType, dir, influence)) {
    			rc.buildRobot(blueprint.robotType, dir, influence);
    			rc.setFlag(assignmentFlag.encode());
    			this.flagCooldown = FLAG_COOLDOWN_START;
    			buildSuccess = true;
    			buildNum++;
    			break;
    		}
    	}
    	UtilBattlecode.log("Build attempted; success: " + buildSuccess);
    	return buildSuccess;
    }
    
    private Blueprint makeBlueprint(Target target) {
    	return new Blueprint(RobotType.POLITICIAN, Assignment.ATTACK_TARGET);
    }
    
    private boolean targetFilter(Target target, RobotController rc) {
    	return (target.robotType == RobotType.ENLIGHTENMENT_CENTER) && !rc.getLocation().equals(target.mapLoc) && !this.addedTargets.contains(target);
    }
    
    private int incrementDegrees() {
    	int nextDegrees = this.nextDegrees;
    	this.nextDegrees += 90;
    	if (this.nextDegrees >= UtilMath.CIRCLE_DEGREES) {
    		this.nextDegrees -= UtilMath.CIRCLE_DEGREES;
    		this.nextDegrees += DEGREES_DELTA;
    	}
    	return nextDegrees;
    }
    
	@Override
	public RobotPlayer.IRobotHandler handle(RobotController rc) throws GameActionException {

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
						this.addedTargets.add(target);
						UtilBattlecode.log("Added target @ " + target.mapLoc);
					}					
				} else if ((this.targetQueue.size() > 0) && Flag.getOpCode(rawFlag) == OpCode.TARGET_MISSING) {
					TargetMissingFlag flag = TargetMissingFlag.decode(rawFlag);
					IntVec2D offset = flag.getCoord();
					MapLocation mapLoc = HandlerCommon.offsetToMapLocation(offset, rc.getLocation());
					if (mapLoc.equals(this.targetQueue.peek().mapLoc)) {
						this.targetQueue.remove();
					}
				}
			} else {
				idIterator.remove();
			}
		}
		
		// select Target / Blueprint, build / deplay
		if (this.buildNum % 2 == 1) {
			if (this.attemptBuild(rc, new Blueprint(RobotType.SLANDERER, Assignment.PATROL), new PatrolAssignmentFlag(this.nextDegrees))) {
				this.incrementDegrees();
			}
		}
		else if (this.targetQueue.size() > 0) {
			UtilBattlecode.log("HAS TARGET");
			Target target = this.targetQueue.peek();
			Blueprint blueprint = this.makeBlueprint(target);
			IntVec2D offset = HandlerCommon.mapLocationToOffset(target.mapLoc);
			Flag.AttackAssignmentFlag flag = new Flag.AttackAssignmentFlag(offset.x, offset.y);
			this.attemptBuild(rc, blueprint, flag);
		} else {
			Blueprint blueprint = new Blueprint(RobotType.POLITICIAN, Assignment.PATROL);
			Flag.PatrolAssignmentFlag flag = new Flag.PatrolAssignmentFlag(this.nextDegrees);
			if (this.attemptBuild(rc, blueprint, flag)) {
				this.incrementDegrees();
			}
			
		}
        
        return this;
	}
}
