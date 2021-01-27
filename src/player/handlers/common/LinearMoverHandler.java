package player.handlers.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import player.util.battlecode.UtilBattlecode;
import player.util.general.UtilGeneral;
import player.util.math.DoubleVec2D;
import player.util.math.Line2D;
import player.util.math.UtilMath;

public class LinearMoverHandler {
	
	private static final double LINE_DIST_THRESH = 2.0;  // TODO(theimer)
	
	private Line2D line;
	private DoubleVec2D vec;
	private boolean endOfLine;
	private boolean isBlocked;
	
	public LinearMoverHandler(Line2D line, DoubleVec2D vec) {
		this.line = line;
		this.vec = vec;
		this.endOfLine = false;
		isBlocked = false;
	}
	
	private boolean canStep(RobotController rc) {
		DoubleVec2D mapLocVec = new DoubleVec2D(rc.getLocation().x, rc.getLocation().y);
		return (!this.endOfLine) && (UtilMath.distanceFromLine(mapLocVec, line) < LINE_DIST_THRESH);
	}
	
	private Set<MapLocation> getProgressMapLocs(MapLocation mapLoc, RobotController rc) throws GameActionException {
		Iterator<MapLocation> adjacentMapLocIterator = UtilBattlecode.makeAdjacentMapLocIterator(mapLoc);
		Stream<MapLocation> mapLocStream = UtilGeneral.streamifyIterator(adjacentMapLocIterator);
		
		Predicate<MapLocation> pred =  UtilBattlecode.<MapLocation>silenceGameActionPredicate(ll -> rc.onTheMap(ll));
		
		return UtilGeneral.legalSetCollect(mapLocStream.filter(loc -> (
					// close enough to the path line?
					(UtilMath.distanceFromLine(new DoubleVec2D(loc.x, loc.y), line) < LINE_DIST_THRESH) &&
					// valid location on the map?
				    pred.test(loc) &&
				    // heading in the right direction?
					(vec.dot(new DoubleVec2D(loc.x - mapLoc.x, loc.y - mapLoc.y)) > 0)
		)));
	}
	
	private Optional<MapLocation> greedyNextLocation(Collection<MapLocation> progressMapLocs, RobotController rc) throws GameActionException {
	    assert !progressMapLocs.isEmpty() : "TODO";
		Predicate<MapLocation> pred = UtilBattlecode.<MapLocation>silenceGameActionPredicate(ll -> !rc.isLocationOccupied(ll));
		Set<MapLocation> mapLocsUnoccupied = UtilGeneral.legalSetCollect(progressMapLocs.stream().filter(pred));
		MapLocation startLoc = rc.getLocation();
		Function<MapLocation, Double> costFunc = new Function<MapLocation, Double>() {
			public Double apply(MapLocation mapLoc) {
				DoubleVec2D mapLocVec = new DoubleVec2D(mapLoc.x, mapLoc.y);
				DoubleVec2D diffVec = new DoubleVec2D(mapLoc.x - startLoc.x,
						                              mapLoc.y - startLoc.y);
				return -diffVec.dot(vec);  // negated for least cost
			}
		};
		if (mapLocsUnoccupied.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(UtilGeneral.findLeastCostLinear(mapLocsUnoccupied.iterator(), costFunc));
	}
	
	public void reverse() {
		this.endOfLine = false;
		this.isBlocked = false;
		this.vec = vec.negate();
	}
	
	public boolean blocked() {
		return this.isBlocked;
	}
	
	public boolean atEndOfLine() {
		return this.endOfLine;
	}
	
	public boolean step(RobotController rc) throws GameActionException {
		assert this.canStep(rc) : "Illegal step (endOfLine:" + this.endOfLine + ")";
		Set<MapLocation> progressMapLocSet = this.getProgressMapLocs(rc.getLocation(), rc);
		boolean stepAttempt;
		if (progressMapLocSet.isEmpty()) {
			this.endOfLine = true;
			stepAttempt = false;
		} else {
			Optional<MapLocation> nextMapLocOptional = greedyNextLocation(progressMapLocSet, rc);
			if (nextMapLocOptional.isPresent()) {
				MapLocation nextLoc = nextMapLocOptional.get();
				Direction dir = rc.getLocation().directionTo(nextLoc);
				HandlerCommon.attemptMove(rc, dir);
				stepAttempt = true;
				this.isBlocked = false;
			} else {
				this.isBlocked = true;
				stepAttempt = false;
			}
		}
		return stepAttempt;
	}
	
}