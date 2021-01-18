package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon;
import util.search.BFSGenerator;
import util.Flag;

import util.Util;
import util.UtilMath;
import util.UtilMath.*;

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

import static org.junit.Assert.assertArrayEquals;
import static player.handlers.HandlerCommon.*;

public class LeaderHandler implements IRobotRoleHandler {
	
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
	
	@FunctionalInterface
	private static interface GameActionPredicate<T> {
		public boolean test(T t) throws GameActionException;
	}
	
	private static <T> boolean safeGameActionEvalPredicate(GameActionPredicate<T> pred, T t) {
		try {
			return pred.test(t);
		} catch (GameActionException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Use of this function implies this should never happen!");
		}
	}
	
	private Set<MapLocation> getProgressMapLocs(MapLocation mapLoc, RobotController rc) throws GameActionException {
		final double lineDistThresh = 2.0;
		Iterator<MapLocation> adjacentMapLocIterator = HandlerCommon.getAdjacentIterator(mapLoc);
		Stream<MapLocation> mapLocStream = Util.streamifyIterator(adjacentMapLocIterator);
		
		Predicate<MapLocation> pred = new Predicate<MapLocation>() {
			@Override
			public boolean test(MapLocation t) {
				return safeGameActionEvalPredicate(ll -> rc.onTheMap(ll), t);
			}
		};
		
		return Util.legalSetCollect(mapLocStream.filter(loc -> (
					// close enough to the path line?
					(UtilMath.distanceFromLine(new DoubleVec2D(loc.x, loc.y), squadState.pathLine) < lineDistThresh) &&
					// valid location on the map?
				    pred.test(loc) &&
				    // heading in the right direction?
					(squadState.pathVec.dot(new DoubleVec2D(loc.x - mapLoc.x, loc.y - mapLoc.y)) > 0)
		)));
	}
	
	private Optional<MapLocation> greedyNextLocation(Collection<MapLocation> progressMapLocs, RobotController rc) throws GameActionException {
		Util.battlecodeAssert(!progressMapLocs.isEmpty(), "TODO", rc);
		Predicate<MapLocation> pred = new Predicate<MapLocation>() {
			@Override
			public boolean test(MapLocation t) {
				return safeGameActionEvalPredicate(ll -> !rc.isLocationOccupied(ll), t);
			}
			
		};
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
	
	private void handlePatrol(RobotController rc) throws GameActionException {
		if (this.needToWaitForSquad(rc)) {
			return;
		}
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
			if (rc.canMove(dir)) {
				rc.move(dir);
			}
		}
	}
	
	private void handleOccupy(RobotController rc) {
		
	}
	
	private static final int SENSE_RADIUS_SQUARED_EPS = 16;
	
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
