package player.handlers.common;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.EnemySightedFlag;
import player.util.general.UtilGeneral;

/*
 * TODO(theimer):
 *     None of these functions are constrained to operate on specific collections of robots
 *     i.e. "only teammates" or "only adjacent", and hence much filtering will be redundant
 *     across separate calls to these functions.
 *
 *     If the Battlecode bytecode limits are reached, constraints should be imposed on
 *     these collections such that the specific RobotType handlers are forced to
 *     filter them (and hence these functions can be called again without additional filtering).
 */

/**
 * Contains functions common among handlers.
 *
 * Note: all "attempt" functions check that a game action is possible before performing the action.
 *     If it's possible, it executes; otherwise, it does not. The functions returns true
 *     iff the action executes.
 */
public class HandlerCommon {

	/**
	 * Attempts to move the robot into the adjacent space as specified by the argument Direction.
	 *
	 * @param rc the RobotController for the current round.
	 * @return true iff the move completes successfully; else false.
	 */
	public static boolean attemptMove(final RobotController rc, final Direction dir) {
		UtilBattlecode.log("Attempting to move Direction: " + dir);
		boolean moveSuccessful;
		if (rc.canMove(dir)) {
			try {
				rc.move(dir);
			} catch (final GameActionException e) {
				// rc::canMove is a precondition of the try-catch.
				throw new UtilBattlecode.IllegalGameActionException(e);
			}
			moveSuccessful = true;
		} else {
			moveSuccessful = false;
		}
		UtilBattlecode.log("Move success: " + moveSuccessful);
		return moveSuccessful;
	}

	/**
	 * Sets the robot's flag.
	 *
	 * @param rc the RobotController for the current round.
	 */
	public static void setFlag(final RobotController rc, final IFlag flag) {
		final int rawFlag = Flag.encode(flag);
		assert rc.canSetFlag(rawFlag);
		try {
			rc.setFlag(rawFlag);
		} catch (final GameActionException e) {
			// rc.canSetFlag is a precondition, so this shouldn't happen!
			throw new UtilBattlecode.IllegalGameActionException(e);
		}
	}

	/**
	 * Returns a stream of RobotInfo/IFlag pairs such that each RobotInfo
	 * robot is on the same Team as the RobotController robot.
	 *
	 * @param rc the RobotController for the current round.
	 * @param robotCollection the robots to be included in the robot/flag pairs.
	 * 	   Must have been sensed by the controller during this round (i.e. the RobotInfos are up-to-date).
	 *     Note that all non-team robots will be automatically filtered from the result.
	 */
	public static Stream<SimpleImmutableEntry<RobotInfo, IFlag>> getTeamFlagStream(final RobotController rc,
			                                                                       final Collection<RobotInfo> robotCollection) {
		return robotCollection.stream()
			// get only the robots on our team
			.filter(PredicateFactories.robotSameTeam(rc.getTeam()))
		    // map to RobotInfo/IFlag pairs
		    .map(new Function<RobotInfo, SimpleImmutableEntry<RobotInfo, IFlag>>() {

				@Override
				public SimpleImmutableEntry<RobotInfo, IFlag> apply(final RobotInfo robotInfo) {
					int rawFlag;
					try {
						rawFlag = rc.getFlag(robotInfo.getID());
					} catch (final GameActionException e) {
						// This should never happen-- sensedRobots must be sensable at the current round.
						throw new UtilBattlecode.IllegalGameActionException(e);
					}
					return new SimpleImmutableEntry<>(robotInfo, Flag.decode(rawFlag));
				}
			});
	}

	/**
	 * Returns the robot nearest to the RobotController robot.
	 *
	 * @param rc the RobotController for the current round.
	 * @param robotCollection the RobotInfos sensed by the RobotController for the current round.
	 *     Must have been sensed by the controller during this round (i.e. the RobotInfos are up-to-date).
	 *     Must have size > 0.
	 */
	public static RobotInfo getNearestRobot(final RobotController rc, final Collection<RobotInfo> robotCollection) {
		assert robotCollection.size() > 0 :
			"robotCollection must have positive size; size: " + robotCollection.size();
		final Function<RobotInfo, Double> costFunc = robotInfo -> (double)rc.getLocation().distanceSquaredTo(robotInfo.getLocation());
		return UtilGeneral.getLeastCostLinear(robotCollection, costFunc);
	}

	/**
	 * Returns some robot/flag pair such that:
	 *     (1) the robot is immediately adjacent to the RobotController robot, and
	 *     (2) the robot is on the same Team as the RobotController robot, and
	 *     (3) the flag is an "assignment" flag.  // TODO(theimer): make this definition more clear/concrete
	 *
	 * @param rc the RobotController for the current round.
	 * @param robotCollection RobotInfos sensed by the RobotController during the current round.
	 *     Note that all non-team robots are automatically filtered.
	 * @return an occupied Optional iff a RobotInfo in the collection has posted a flag, and the
	 *     RobotInfo/IFlag pair meets the above three criteria.
	 */
	public static Optional<SimpleImmutableEntry<RobotInfo, IFlag>> getAnyAdjacentAssignmentFlag(final RobotController rc,
			                                                                                    final Collection<RobotInfo> robotCollection) {
		return
				// get only flags for the robots on our team
				HandlerCommon.getTeamFlagStream(rc, robotCollection)
				// filter out non-adjacent robots
				.filter(entry -> PredicateFactories.robotAdjacentTo(rc.getLocation()).test(entry.getKey()))
				// filter out non-assignment flags
				.filter(entry -> PredicateFactories.flagAssignment().test(entry.getValue()))
				.findAny();
	}

	/**
	 * Performs three actions:
	 *
	 * (1) Finds the non-teammate robots in a collection.
	 * (2) Assigns each a 'priority'.
	 * (3) Sets an EnemySightedFlag to signal the non-teammate robot of highest priority.
	 *
	 * If no non-teammate robot is found, (2) and (3) do not execute.
	 *
	 * @param rc the RobotController for the current round.
	 * @param robotCollection the collection of robots to search through.
	 *     Must have been sensed during the current round (i.e. the RobotInfos are up-to-date).
	 *     Note that teammate robots are automatically ignored.
	 * @return true iff a flag is set; else false.
	 */
	public static boolean setHighestPriorityEnemySightedFlag(final RobotController rc, final Collection<RobotInfo> robotCollection) {

		/* The three steps of this function occur verbatim in all non-Enlightenment-Center handlers. */

		// TODO(theimer): improve this cost Function; make unique to the controller?
		final Function<RobotInfo, Double> costFunc = new Function<RobotInfo, Double>(){
			@Override
			public Double apply(final RobotInfo robotInfo) {
				switch(robotInfo.getType()) {
					// costs are negated since we use a least-cost utility function.
					case POLITICIAN: return -1.0;
					case MUCKRAKER: return -2.0;
					case SLANDERER: return -3.0;
					case ENLIGHTENMENT_CENTER: return -4.0;
					default: throw new IllegalArgumentException("unrecognized RobotInfo: " + robotInfo.getType());
				}
			}
		};

		// arbitrarily assume the flag is not set.
		boolean flagSet = false;

		// get all non-teammate robots
		final List<RobotInfo> nonTeamRobotList = new ArrayList<>();
		{
			final Stream<RobotInfo> nonTeamRobotStream = robotCollection.stream().filter(PredicateFactories.robotNonTeam(rc.getTeam()));
			UtilGeneral.legalCollect(nonTeamRobotStream, nonTeamRobotList);
		}

		if (nonTeamRobotList.size() > 0) {
			// find the one of highest priority
			final RobotInfo highestPriorityRobot = UtilGeneral.getLeastCostLinear(nonTeamRobotList, costFunc);
			// post the flag!
			final EnemySightedFlag flag = new EnemySightedFlag(highestPriorityRobot.getType(), highestPriorityRobot.getLocation());
			HandlerCommon.setFlag(rc, flag);
			flagSet = true;
		}

		return flagSet;
	}

	/**
	 * Returns the Direction a robot should move in order to occupy the adjacent,
	 * least-cost MapLocation that is:
	 *     (1) on the map, and
	 *     (2) unoccupied.
	 * @param rc the RobotController for the current round.
	 * @param costFunc a function that maps all MapLocations to a cost value.
	 * @return an occupied Optional iff at least one adjacent MapLocation satisfies
	 *     the above two constraints; else an empty Optional.
	 */
	public static Optional<Direction> getLeastCostMoveDirection(final RobotController rc, final Function<MapLocation, Double> costFunc) {
		// get all adjacent/on-the-map/unoccupied MapLocations.
		final List<MapLocation> validAdjacentList = new ArrayList<>();
		{
			final Stream<MapLocation> validAdjacentStream =
					UtilBattlecode.makeAllAdjacentMapLocStream(rc.getLocation())
					// note: silenced predicates will never throw GameActionExceptions because the MapLocations
					//     are adjacent (i.e. senseable).
					.filter(PredicateFactories.mapLocOnMapSilenced(rc))
					.filter(PredicateFactories.mapLocUnoccupiedSilenced(rc));
			UtilGeneral.legalCollect(validAdjacentStream, validAdjacentList);
		}

		Optional<Direction> result;
		if (validAdjacentList.size() > 0) {
			// convert the least-cost MapLoc to a Direction
			final MapLocation leastCostMapLoc = UtilGeneral.getLeastCostLinear(validAdjacentList, costFunc);
			result = Optional.of(rc.getLocation().directionTo(leastCostMapLoc));
		} else {
			result = Optional.empty();
		}
		return result;
	}
}
