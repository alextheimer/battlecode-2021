package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon;
import player.handlers.HandlerCommon.RobotState;
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
	private static final int CLAIM_COOLDOWN_START = 1;  // 1 full round for all bots to see flag
	
	private List<IntVec2D> plannedPath;
	private PathDirection pathDirection;
	private int claimCooldown;
	
	public LeaderHandler() {
		plannedPath = null;
		pathDirection = PathDirection.NONE;
		claimCooldown = CLAIM_COOLDOWN_START;
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
	
	private static <T> Set<T> legalSetCollect(Stream<T> stream) {
		Iterator<T> iterator = stream.iterator();
		Set<T> resultSet = new HashSet<>();
		while (iterator.hasNext()) {
			resultSet.add(iterator.next());
		}
		return resultSet;
	}
	
	private Set<MapLocation> getProgressMapLocs(MapLocation mapLoc, RobotController rc, RobotState state) throws GameActionException {
		final double lineDistThresh = 2.0;
		Iterator<MapLocation> adjacentMapLocIterator = HandlerCommon.getAdjacentIterator(mapLoc);
		Stream<MapLocation> mapLocStream = Util.streamifyIterator(adjacentMapLocIterator);
		
		Predicate<MapLocation> pred = new Predicate<MapLocation>() {
			@Override
			public boolean test(MapLocation t) {
				return safeGameActionEvalPredicate(ll -> rc.onTheMap(ll), t);
			}
		};
		
		return legalSetCollect(mapLocStream.filter(loc -> (
					// close enough to the path line?
					(UtilMath.distanceFromLine(new DoubleVec2D(loc.x, loc.y), state.orders.pathLine) < lineDistThresh) &&
					// valid location on the map?
				    pred.test(loc) &&
				    // heading in the right direction?
					(state.orders.pathVec.dot(new DoubleVec2D(loc.x - mapLoc.x, loc.y - mapLoc.y)) > 0)
		)));
	}
	
	private Optional<MapLocation> greedyNextLocation(Collection<MapLocation> progressMapLocs, RobotController rc, RobotState state) throws GameActionException {
		assert !progressMapLocs.isEmpty();
		Predicate<MapLocation> pred = new Predicate<MapLocation>() {
			@Override
			public boolean test(MapLocation t) {
				return safeGameActionEvalPredicate(ll -> !rc.isLocationOccupied(ll), t);
			}
			
		};
		Set<MapLocation> mapLocsUnoccupied = legalSetCollect(progressMapLocs.stream().filter(pred));
		MapLocation startLoc = rc.getLocation();
		Function<MapLocation, Double> costFunc = new Function<MapLocation, Double>() {
			public Double apply(MapLocation mapLoc) {
				DoubleVec2D mapLocVec = new DoubleVec2D(mapLoc.x, mapLoc.y);
				DoubleVec2D diffVec = new DoubleVec2D(mapLoc.x - startLoc.x,
						                              mapLoc.y - startLoc.y);
				return -diffVec.dot(state.orders.pathVec);  // negated for least cost
			}
		};
		if (mapLocsUnoccupied.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(Util.findLeastCostLinear(mapLocsUnoccupied.iterator(), costFunc));
	}
	
	private void handlePatrol(RobotController rc, RobotState state) throws GameActionException {
		Set<MapLocation> progressMapLocSet = this.getProgressMapLocs(rc.getLocation(), rc, state);
		if (progressMapLocSet.isEmpty()) {
			state.orders.pathVec = state.orders.pathVec.negate();
			progressMapLocSet = this.getProgressMapLocs(rc.getLocation(), rc, state);
		}
		assert !progressMapLocSet.isEmpty();
		Optional<MapLocation> nextMapLocOptional = greedyNextLocation(progressMapLocSet, rc, state);
		if (nextMapLocOptional.isPresent()) {
			MapLocation nextLoc = nextMapLocOptional.get();
			Direction dir = rc.getLocation().directionTo(nextLoc);
			if (rc.canMove(dir)) {
				rc.move(dir);
			}
		}
	}
	
	private void handleOccupy(RobotController rc, RobotState state) {
		
	}
	
	@Override
	public IRobotRoleHandler handle(RobotController rc, RobotState state) throws GameActionException {
		// TODO(theimer): remove these checks?
		if (this.claimCooldown > 0) {
			this.claimCooldown--;
			return this;
		}
		if (this.claimCooldown == 0) {
			this.claimCooldown--;
			rc.setFlag(Flag.EMPTY_FLAG);
		}
		
		switch(state.orders.squadType) {
		case PATROL:
			handlePatrol(rc, state);
			break;
		case OCCUPY:
//			handleOccupy(RobotController rc, RobotState state);
			break;
		case NONE:
		case UNASSIGNED:
			assert false : "leaders shouldn't have these SquadTypes!";
			break;
		}
		return this;
	}

}
