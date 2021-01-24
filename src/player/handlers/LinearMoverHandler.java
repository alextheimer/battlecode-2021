package player.handlers;

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
import player.util.Util;
import player.util.UtilMath;
import player.util.UtilMath.DoubleVec2D;
import player.util.UtilMath.Line2D;

public class LinearMoverHandler {
	
	private static final double LINE_DIST_THRESH = 2.0;  // TODO(theimer)
	
	private Line2D line;
	private DoubleVec2D vec;
	private boolean endOfLine;
	
	public LinearMoverHandler(Line2D line, DoubleVec2D vec) {
		this.line = line;
		this.vec = vec;
		this.endOfLine = false;
	}
	
	private boolean canStep(RobotController rc) {
		DoubleVec2D mapLocVec = new DoubleVec2D(rc.getLocation().x, rc.getLocation().y);
		return (!this.endOfLine) && (UtilMath.distanceFromLine(mapLocVec, line) < LINE_DIST_THRESH);
	}
	
	private Set<MapLocation> getProgressMapLocs(MapLocation mapLoc, RobotController rc) throws GameActionException {
		Iterator<MapLocation> adjacentMapLocIterator = HandlerCommon.getAdjacentIterator(mapLoc);
		Stream<MapLocation> mapLocStream = Util.streamifyIterator(adjacentMapLocIterator);
		
		Predicate<MapLocation> pred =  HandlerCommon.<MapLocation>wrapGameActionPredicate(ll -> rc.onTheMap(ll));
		
		return Util.legalSetCollect(mapLocStream.filter(loc -> (
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
		Predicate<MapLocation> pred = HandlerCommon.<MapLocation>wrapGameActionPredicate(ll -> !rc.isLocationOccupied(ll));
		Set<MapLocation> mapLocsUnoccupied = Util.legalSetCollect(progressMapLocs.stream().filter(pred));
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
		return Optional.of(Util.findLeastCostLinear(mapLocsUnoccupied.iterator(), costFunc));
	}
	
	public void reverse() {
		this.endOfLine = false;
		this.vec = vec.negate();
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
			} else {
				stepAttempt = false;
			}
		}
		return stepAttempt;
	}
	
}
