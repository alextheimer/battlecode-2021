package player.handlers.robots;

import battlecode.common.*;
import player.handlers.common.HandlerCommon;
import player.handlers.common.HandlerCommon.IRobotHandler;
import player.util.battlecode.Flag;
import player.util.battlecode.Flag.EnemySightedFlag;
import player.util.battlecode.Flag.OpCode;
import player.util.battlecode.Flag.PatrolAssignmentFlag;
import player.util.general.UtilGeneral;
import player.util.math.UtilMath;

import java.util.AbstractMap.SimpleImmutableEntry;

import static player.handlers.common.HandlerCommon.*;

import java.util.Iterator;
import java.util.Optional;

public class SlandererHandler implements IRobotHandler {
	
	private MapLocation origin = null;  // TODO(theimer): should be in constructor / non-nullable
	
	public SlandererHandler() {
	}
	
	private Optional<PatrolAssignmentFlag> findAssignmentFlag(RobotController rc) throws GameActionException {
		Optional<SimpleImmutableEntry<RobotInfo, Integer>> entry = HandlerCommon.findFirstMatchingTeamFlag(rc, rc.senseNearbyRobots(), 
				(robotInfo, rawFlag) -> Flag.getOpCode(rawFlag) == OpCode.ASSIGN_PATROL);
		return entry.isPresent() ? Optional.of(PatrolAssignmentFlag.decode(entry.get().getValue())) : Optional.empty();
	}
	
	@Override
	public IRobotHandler handle(RobotController rc) throws GameActionException {
		if (rc.getType() != RobotType.SLANDERER) {
			return new PoliticianHandler();
		}
		
		if (this.origin == null) {
			this.origin = rc.getLocation();
		}
		
        Optional<RobotInfo> sensedEnlightenmentCenterOpt = HandlerCommon.senseAllNonTeam(rc).stream()
        		.filter(robotInfo -> robotInfo.getType() == RobotType.ENLIGHTENMENT_CENTER).findAny();
        if(sensedEnlightenmentCenterOpt.isPresent()) {
        	RobotInfo enlightenmentCenterInfo = sensedEnlightenmentCenterOpt.get();
        	EnemySightedFlag flag = new EnemySightedFlag(RobotType.ENLIGHTENMENT_CENTER, enlightenmentCenterInfo.getLocation().x, enlightenmentCenterInfo.getLocation().y);
        	rc.setFlag(flag.encode());
        }
        
        Optional<RobotInfo> nearestOpt = HandlerCommon.senseNearestNonTeam(rc, rc.senseNearbyRobots());
        if (nearestOpt.isPresent()) {
        	MapLocation enemyLoc = nearestOpt.get().getLocation();
        	Iterator<MapLocation> mapLocIter = HandlerCommon.getAdjacentIterator(rc.getLocation());
        	Iterator<MapLocation> filteredMapLocIter = UtilGeneral.streamifyIterator(mapLocIter)
        			.filter(HandlerCommon.wrapGameActionPredicate(mapLoc -> rc.onTheMap(mapLoc) && !rc.isLocationOccupied(mapLoc))).iterator();
        	if (filteredMapLocIter.hasNext()) {
        		MapLocation moveToLoc = UtilGeneral.findLeastCostLinear(filteredMapLocIter, mapLoc -> (double)-mapLoc.distanceSquaredTo(enemyLoc));
        		HandlerCommon.attemptMove(rc, rc.getLocation().directionTo(moveToLoc));        		
        	}
        } else {
        	Iterator<MapLocation> mapLocIter = HandlerCommon.getAdjacentIterator(rc.getLocation());
        	Iterator<MapLocation> filteredMapLocIter = UtilGeneral.streamifyIterator(mapLocIter)
        			.filter(HandlerCommon.wrapGameActionPredicate(mapLoc -> rc.onTheMap(mapLoc) && !rc.isLocationOccupied(mapLoc))).iterator();
        	if (filteredMapLocIter.hasNext()) {
        		
        		MapLocation moveToLoc = UtilGeneral.findLeastCostLinear(filteredMapLocIter, mapLoc -> (double)-mapLoc.distanceSquaredTo(this.origin));
        		HandlerCommon.attemptMove(rc, rc.getLocation().directionTo(moveToLoc));        		
        	}
        }
        
        return this;
	}
}