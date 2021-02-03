package player.handlers.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
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
	
	// the maximum distance the robot is allowed to move from the line.
	public static final double MAX_LINE_DIST = 2.0;
	
	// the line to move the robot along.
	private Line2D line;
	// The direction to move the robot along the line (parallel to `line`).
	private DoubleVec2D vec;
	// true iff the most recent move attempt failed because no valid locations exist (including occupied / 
	// temporarily blocked locations) further along `line` in the direction of `vec`.
	private boolean atEndOfLine;
	// true iff !atEndOfLine but no traversable locations exist (i.e. they are occupied).
	private boolean isBlocked;
	
	private void assertRepCheck() {
		assert !(atEndOfLine && isBlocked) :
			String.format("can't be blocked at the end of the line; endOfLine: %b; isBlocked: %b", atEndOfLine, isBlocked);
		assert UtilMath.vecParallelToLine(vec, line) : 
			"vec must be parallel to line; vec: " + vec + ", line: " + line;
	}
	
	/**
	 * Handles robot movement along a linear path.
	 * 
	 * @param line the line along which the robot is moved.
	 * @param vec the direction in which the robot should move along the line.
	 *     Must be parallel to `line`.
	 */
	public LinearMoverHandler(Line2D line, DoubleVec2D vec) {
		assert UtilMath.vecParallelToLine(vec, line) : 
			"vec must be parallel to line; vec: " + vec + ", line: " + line;
		this.line = line;
		this.vec = vec;
		this.atEndOfLine = false;
		this.isBlocked = false;
		assertRepCheck();
	}
	
	/**
	 * Returns a Stream of all locations that the RobotController can move into such that each:
	 *     (1) is adjacent to the robot's current location, and
	 *     (2) is on the map, and
	 *     (3) would progress the robot along this.line in the direction of this.vec, and
	 *     (4) is within MAX_LINE_DIST of the this.line.
	 * 
	 * @param rc the RobotController for the current round.
	 *     Must have a current location within distance MAX_LINE_DIST of this.line.
	 */
	private Stream<MapLocation> getProgressiveMapLocStream(RobotController rc) {
		MapLocation currentMapLoc = rc.getLocation();
		
		assert UtilMath.distanceFromLine(new DoubleVec2D(currentMapLoc.x, currentMapLoc.y), line) < MAX_LINE_DIST : 
			"the robot's current MapLocation must lie less than MAX_LINE_DIST distance from the line: location:\n" +
		    rc.getLocation() + ", line: " + this.line;
		
		// is the dot product of the movement diff and this.vec > 0?
		Predicate<MapLocation> progressivePredicate =
				mapLoc -> vec.dot(new DoubleVec2D(mapLoc.x - currentMapLoc.x, mapLoc.y - currentMapLoc.y)) > 0;
		// is the next MapLocation close enough to the line?
		Predicate<MapLocation> nearLinePredicate =
				mapLoc -> UtilMath.distanceFromLine(new DoubleVec2D(mapLoc.x, mapLoc.y), line) < MAX_LINE_DIST;
		
		return UtilBattlecode.makeAllAdjacentMapLocStream(currentMapLoc)
				// on the map?
				// note: this will never throw GameActionExceptions because the MapLocations
				//     are adjacent (i.e. senseable).
				.filter(PredicateFactories.mapLocOnMapSilenced(rc))
				// heading in the right direction?
				.filter(progressivePredicate)
				// acceptable distance from the line?
				.filter(nearLinePredicate);
	}
	
	/**
	 * Selects the robot's "best" next MapLocation from a stream of available MapLocations.
	 * TODO(theimer): make "best" concrete.
	 * 
	 * @param candidateCOllection a non-empty collection of MapLocations such that each is:
	 *     (1) on the map, and
	 *     (2) within MAX_LINE_DIST of this.line.
	 * @param rc the RobotController for the current round.
	 * @return the "best" MapLocation that the robot should next move into to make progress along this.line.
	 */
	private MapLocation selectCandidateMoveMapLocation(Collection<MapLocation> candidateCollection, RobotController rc) {
		assert candidateCollection.size() > 0 : "collection size must be positive; size: " + candidateCollection.size();
		MapLocation currentMapLoc = rc.getLocation();
		Function<MapLocation, Double> progressCostFunc = new Function<MapLocation, Double>() {
			public Double apply(MapLocation mapLoc) {
				// TODO(theimer): assert the validity of the MapLocation (near line, on the map)
				DoubleVec2D diffVec = new DoubleVec2D(mapLoc.x - currentMapLoc.x,
						                              mapLoc.y - currentMapLoc.y);
				return -diffVec.dot(vec);  // negated for least cost
			}
		};
		return UtilGeneral.getLeastCostLinear(candidateCollection, progressCostFunc);
	}
	
	/**
	 * Reverses the direction the handler moves the robot along its line.
	 * 
	 * Note: blocked() and endOfLine() are reset when this is called (they will return false
	 * until a call to attemptStep() warrants otherwise).
	 */
	public void reverse() {
		this.atEndOfLine = false;
		this.isBlocked = false;
		this.vec = vec.negate();
		assertRepCheck();
	}
	
	/**
	 * Returns true if step() was attempted and failed because:
	 *     (1) candidate move() MapLocations existed, but
	 *     (2) all were occupied.
	 *     
	 * Note: this is reset when this reverse() called (it returns false
	 * until a call to attemptStep() warrants otherwise).
	 */
	public boolean blocked() {
		return this.isBlocked;
	}
	
	/**
	 * Returns true if step() was attempted and failed because
	 * no candidate move() MapLocations existed.
	 * 
	 * Note: this is reset when this reverse() called (it returns false
	 * until a call to attemptStep() warrants otherwise).
	 */
	public boolean endOfLine() {
		return this.atEndOfLine;
	}
	
	/**
	 * Attempts to step the robot into an adjacent, unoccupied MapLocation result_step_location such that:
	 *     (1) the location is within MAX_LINE_DIST distance of the line.
	 *     (2) the dot product of the current movement vector and the difference vector
	 *         (result_step_location - rc.getLocation()) is positive.
	 * 
	 * Preconditions:
	 * 	   - !blocked()
	 *     - !atEndOfLine()
	 * 
	 * @param rc the RobotController for the current round.
	 * @return true iff a step is attempted (Note: not necessarily completed)
	 */
	public boolean attemptStep(RobotController rc) {
		assert !(this.isBlocked || this.atEndOfLine) : "the robot must not be ";
		
		// arbitrarily assume no step is attempted
		boolean stepAttempted = false;

		// get only the adjacent/unoccupied/on-the-map MapLocations that satisfy both constraints
		Iterator<MapLocation> progressiveMapLocIterator = this.getProgressiveMapLocStream(rc).iterator();
			
		//lazily check the existence of any of these "progressive" locations
		if (progressiveMapLocIterator.hasNext()) {
			
			// collect all progressive locations that are unoccupied
			List<MapLocation> candidateList = new ArrayList<>();
			progressiveMapLocIterator.forEachRemaining(new Consumer<MapLocation>() {
				
				// see below: this won't ever throw a GameActionException because it will only ever
				// be called on adjacent (i.e. senseable) MapLocations.
				Predicate<MapLocation> unoccupiedPredicate = PredicateFactories.mapLocOnMapSilenced(rc);
				
				@Override
				public void accept(MapLocation t) {
					if (unoccupiedPredicate.test(t)) {
						candidateList.add(t);
					}
				}
			});
			
			if (candidateList.size() > 0) {
				// unoccupied/progressive locations exist!
				// pick the best and attempt a move.
				MapLocation moveLoc = selectCandidateMoveMapLocation(candidateList, rc);
				HandlerCommon.attemptMove(rc, rc.getLocation().directionTo(moveLoc));
				stepAttempted = true;
			} else {
				// progressive locations exist, but unoccupied locations don't
				this.isBlocked = true;
			}
		} else {
			// no progressive locations exist
			this.atEndOfLine = true;
		}
		assertRepCheck();
		return stepAttempted;
	}
	
}
