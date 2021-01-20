package player.handlers;

import battlecode.common.*;
import player.util.search.BFSGenerator;
import player.util.Flag;
import player.util.Flag.AttackTargetFlag;
import player.util.Util;
import player.util.UtilMath;
import player.util.UtilMath.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import player.handlers.HandlerCommon.GameActionPredicate;

import static org.junit.Assert.assertArrayEquals;
import static player.handlers.HandlerCommon.*;

public class LeaderHandler implements IRobotRoleHandler {
	
	private static final int SENSE_RADIUS_SQUARED_EPS = 16;
	private static final double LINE_DIST_THRESH = 2.0;  // TODO(theimer)
	
	private static enum PathDirection {INBOUND, OUTBOUND, NONE};
	
	private List<IntVec2D> plannedPath;
	private PathDirection pathDirection;
	private SquadState squadState;
	
	public LeaderHandler(SquadState squadState) {
		this.squadState = squadState;
		plannedPath = null;
		pathDirection = PathDirection.NONE;
	}
	
//	private static Set<IntVec2D> expandCoord(IntVec2D coord, RobotController rc) {
//		return Util.makeAllAdjacentStream(coord).filter(
//				expCoord ->
//					((expCoord.x != coord.x) ^ (expCoord.y != coord.y)) &&  // only cardinal
//					(rc.canSenseLocation(new MapLocation(coord.x, coord.y)))  // sensable location
//				).collect(Collectors.toSet());
//	}
	
//	private Map<IntVec2D, Double> scanPassabilities(RobotController rc) {
//		MapLocation currLoc = rc.getLocation();
//		Map<IntVec2D, Double> passabilityMap = new HashMap<>();
//		BFSGenerator<IntVec2D> bfsGen = new BFSGenerator<>(new IntVec2D(currLoc.x, currLoc.y), coord -> expandCoord(coord, rc));
//		for (IntVec2D vec = bfsGen.next(); vec != null; vec = bfsGen.next()) {
//			try {
//				passabilityMap.put(vec, rc.sensePassability(new MapLocation(vec.x, vec.y)));
//			} catch (GameActionException e) {
//				assert false : "expandCoord should prevent this!";
//			}
//		}
//		return passabilityMap;
//	}
	
	private Set<MapLocation> getProgressMapLocs(MapLocation mapLoc, RobotController rc) throws GameActionException {
		Iterator<MapLocation> adjacentMapLocIterator = HandlerCommon.getAdjacentIterator(mapLoc);
		Stream<MapLocation> mapLocStream = Util.streamifyIterator(adjacentMapLocIterator);
		
		Predicate<MapLocation> pred =  HandlerCommon.<MapLocation>wrapGameActionPredicate(ll -> rc.onTheMap(ll));
		
		return Util.legalSetCollect(mapLocStream.filter(loc -> (
					// close enough to the path line?
					(UtilMath.distanceFromLine(new DoubleVec2D(loc.x, loc.y), squadState.pathLine) < LINE_DIST_THRESH) &&
					// valid location on the map?
				    pred.test(loc) &&
				    // heading in the right direction?
					(squadState.pathVec.dot(new DoubleVec2D(loc.x - mapLoc.x, loc.y - mapLoc.y)) > 0)
		)));
	}
	
	private Optional<MapLocation> greedyNextLocation(Collection<MapLocation> progressMapLocs, RobotController rc) throws GameActionException {
		Util.battlecodeAssert(!progressMapLocs.isEmpty(), "TODO", rc);
		Predicate<MapLocation> pred = HandlerCommon.<MapLocation>wrapGameActionPredicate(ll -> !rc.isLocationOccupied(ll));
		Set<MapLocation> mapLocsUnoccupied = Util.legalSetCollect(progressMapLocs.stream().filter(pred));
		MapLocation startLoc = rc.getLocation();
		Function<MapLocation, Double> costFunc = new Function<MapLocation, Double>() {
			public Double apply(MapLocation mapLoc) {
				DoubleVec2D mapLocVec = new DoubleVec2D(mapLoc.x, mapLoc.y);
				DoubleVec2D diffVec = new DoubleVec2D(mapLoc.x - startLoc.x,
						                              mapLoc.y - startLoc.y);
				return -diffVec.dot(squadState.pathVec);  // negated for least cost
			}
		};
		if (mapLocsUnoccupied.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(Util.findLeastCostLinear(mapLocsUnoccupied.iterator(), costFunc));
	}
	
	private Optional<RobotInfo> findNearestEnemy(RobotController rc, RobotInfo[] nearbyRobots) {
		Iterator<RobotInfo> enemyIterator = Arrays.stream(nearbyRobots).filter(robotInfo -> robotInfo.getTeam() == rc.getTeam()).iterator();
		if (enemyIterator.hasNext()) {
			Function<RobotInfo, Double> costFunc = robotInfo -> (double)robotInfo.getLocation().distanceSquaredTo(rc.getLocation());
			return Optional.of(Util.findLeastCostLinear(enemyIterator, costFunc));
		} else {
			return Optional.empty();
		}
	}
	
	private void handlePatrol(RobotController rc) throws GameActionException {
		// handle targeting/flag stuff first
		if (this.squadState.targetIdOpt.isPresent()) {
			// existing target-- make sure it's still senseable
			int targetId = this.squadState.targetIdOpt.get();
			if (!rc.canSenseRobot(targetId)) {
				Util.battlecodeAssert(rc.getFlag(rc.getID()) != Flag.EMPTY_FLAG, "flag shouldn't be empty!", rc);
				rc.setFlag(Flag.EMPTY_FLAG);
				this.squadState.targetIdOpt = Optional.empty();
			}
		} else {
			// no existing target-- attempt to acquire a new one
			Optional<RobotInfo> newTargetRobotInfoOpt = this.findNearestEnemy(rc, rc.senseNearbyRobots());
			if (newTargetRobotInfoOpt.isPresent()) {
				// found one! store it and tell the squad to attack it.
				RobotInfo newTargetRobotInfo = newTargetRobotInfoOpt.get();
				this.squadState.targetIdOpt = Optional.of(newTargetRobotInfo.getID());
				MapLocation newTargetMapLoc = newTargetRobotInfo.getLocation();
				MapLocation currentMapLoc = rc.getLocation();
				Direction dirToEnemy = currentMapLoc.directionTo(newTargetMapLoc);
				AttackTargetFlag flag = new AttackTargetFlag(newTargetRobotInfo.getID(), dirToEnemy);
				Util.battlecodeAssert(rc.getFlag(rc.getID()) == Flag.EMPTY_FLAG, "flag should be empty!", rc);
				rc.setFlag(flag.encode());
			}
		}
		
		if (this.needToWaitForSquad(rc)) {
			// intentionally blank-- movement not allowed!
//		} else if (this.squadState.targetIdOpt.isPresent()) {
//			// we have a target; move closer if necessary for action
//			//TODO(theimer): this breaks the whole role-/type-handler separation
//			int maxActionDistSquared = rc.getType().actionRadiusSquared;
//			RobotInfo targetInfo = rc.senseRobot(this.squadState.targetIdOpt.get());
//			int distSquared = rc.getLocation().distanceSquaredTo(targetInfo.getLocation());
//			if (distSquared > maxActionDistSquared) {
//				// too far! attempt to move closer...
//				Optional<MapLocation> nextLocOpt = HandlerCommon.getAdjacentCloserTraversableMapLocation(rc.getLocation(), targetInfo.getLocation(), rc);
//				if (nextLocOpt.isPresent()) {
//					rc.move(rc.getLocation().directionTo(nextLocOpt.get()));
//				}
//			}
//		} else if () {
		} else {
			// we don't have a target-- continue along path
			Set<MapLocation> progressMapLocSet = this.getProgressMapLocs(rc.getLocation(), rc);
			if (progressMapLocSet.isEmpty()) {
				squadState.pathVec = squadState.pathVec.negate();
				progressMapLocSet = this.getProgressMapLocs(rc.getLocation(), rc);
			}
			Util.battlecodeAssert(!progressMapLocSet.isEmpty(), "TODO", rc);
			Optional<MapLocation> nextMapLocOptional = greedyNextLocation(progressMapLocSet, rc);
			if (nextMapLocOptional.isPresent()) {
				MapLocation nextLoc = nextMapLocOptional.get();
				Direction dir = rc.getLocation().directionTo(nextLoc);
				HandlerCommon.attemptMove(rc, dir);
			}
		}
	}
	
	private void handleOccupy(RobotController rc) {
		
	}
	
	private boolean needToWaitForSquad(RobotController rc) throws GameActionException {
		for (Integer robotId : this.squadState.squadIdSet) {
			Util.battlecodeAssert(rc.canSenseRobot(robotId), "TODO", rc);
			RobotInfo robotInfo = rc.senseRobot(robotId);
			int minSenseDistSquared = Math.min(rc.getType().sensorRadiusSquared, robotInfo.getType().sensorRadiusSquared);
			int distSquaredBetween = robotInfo.getLocation().distanceSquaredTo(rc.getLocation());
			if (distSquaredBetween > (minSenseDistSquared - SENSE_RADIUS_SQUARED_EPS)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IRobotRoleHandler handle(RobotController rc) throws GameActionException {
		// keep track of living squad members
		Predicate<Integer> predicate = robotId -> !rc.canSenseRobot(robotId);
		Set<Integer> removedIds = Util.removeMatching(squadState.squadIdSet, predicate);
		switch(this.squadState.squadType) {
			case PATROL:
				handlePatrol(rc);
				break;
			case OCCUPY:
	//			handleOccupy(RobotController rc, RobotState state);
				break;
			case NONE:
			case UNASSIGNED:
				Util.battlecodeAssert(false, "leaders shouldn't have these SquadTypes!", rc);
				break;
		}
		return this;
	}

}
