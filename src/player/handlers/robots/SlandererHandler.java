package player.handlers.robots;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import player.RobotPlayer;
import player.handlers.common.HandlerCommon;
import player.handlers.common.PredicateFactories;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.general.UtilGeneral;

public class SlandererHandler implements RobotPlayer.IRobotHandler {

	// the MapLocation of the Enlightenment Center from which the Slanderer originates.
	private final MapLocation originEnlightenmentCenterMapLoc;

	/**
	 * Handles a Slanderer robot.
	 *
	 * @param rc the RobotController for the current round.
	 */
	public SlandererHandler(final RobotController rc) {
		final List<RobotInfo> sensedRobots = Arrays.asList(rc.senseNearbyRobots());
		// look for an assignment flag
		final Optional<SimpleImmutableEntry<RobotInfo, IFlag>> assignmentEntry =
				HandlerCommon.getAnyAdjacentAssignmentFlag(rc, sensedRobots);

		assert assignmentEntry.isPresent() : "no assignment flag detected!";

		// store the origin location
		final RobotInfo originEnlightenmentCenter = assignmentEntry.get().getKey();
		this.originEnlightenmentCenterMapLoc = originEnlightenmentCenter.getLocation();

		// TODO(theimer): handle the assignment type
	}

	@Override
	public RobotPlayer.IRobotHandler handle(final RobotController rc) {

		final List<RobotInfo> sensedRobots = Arrays.asList(rc.senseNearbyRobots());

		// call out the highest priority enemy to any listening enlightenment centers
		HandlerCommon.setHighestPriorityEnemySightedFlag(rc, sensedRobots);

		// find all non-teammates
		final List<RobotInfo> nonTeamRobots = new ArrayList<>();
        {
        	final Stream<RobotInfo> nonTeamRobotStream =
        			sensedRobots.stream()
        			.filter(PredicateFactories.robotNonTeam(rc.getTeam()));
        	UtilGeneral.legalCollect(nonTeamRobotStream, nonTeamRobots);
        }

        // we'll always try to move away from some location
        MapLocation moveAwayFrom;
        if (nonTeamRobots.size() > 0) {
        	// move away from an enemy (this is a non-combatant type)
        	final RobotInfo nearestNonTeamRobot = HandlerCommon.getNearestRobot(rc, nonTeamRobots);
        	moveAwayFrom = nearestNonTeamRobot.getLocation();
        } else {
        	// move away from the enlightenment center (to create space for building)
        	moveAwayFrom = this.originEnlightenmentCenterMapLoc;
        }

        // attempt to move as far from moveAwayFrom as possible
        final Function<MapLocation, Double> distanceCostFunc =
        		mapLoc -> (double)-mapLoc.distanceSquaredTo(moveAwayFrom);  // note: negated!
    	final Optional<Direction> awayDirectionOpt = HandlerCommon.getLeastCostMoveDirection(rc, distanceCostFunc);
    	if (awayDirectionOpt.isPresent()) {
    		HandlerCommon.attemptMove(rc, awayDirectionOpt.get());
    	} // else no adjacent/on-the-map/unoccupied spaces exist

        return this;
	}
}