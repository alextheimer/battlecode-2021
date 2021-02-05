package player.handlers.common;

import java.util.function.Predicate;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;

/**
 * Contains factory functions for common Predicates used to filter
 * Streams of various Battlecode types.
 */
public class PredicateFactories {

	// max distance squared between two adjacent MapLocations
	private final static int MAX_ADJACENT_DIST_SQUARED = 2;

	public static Predicate<RobotInfo> robotAdjacentTo(final MapLocation adjacentTo) {
		return robotInfo -> robotInfo.getLocation()
				.isWithinDistanceSquared(adjacentTo, PredicateFactories.MAX_ADJACENT_DIST_SQUARED);
	}

	public static Predicate<RobotInfo> robotAtMapLocation(final MapLocation mapLoc) {
		return robotInfo -> robotInfo.getLocation().equals(mapLoc);
	}

	public static Predicate<RobotInfo> robotInRange(final MapLocation mapLoc, final int radiusSquared) {
		return robotInfo -> robotInfo.getLocation().distanceSquaredTo(mapLoc) <= radiusSquared;
	}

	public static Predicate<RobotInfo> robotSameTeam(final Team team) {
		return robotInfo -> robotInfo.getTeam() == team;
	}

	public static Predicate<RobotInfo> robotNonTeam(final Team team) {
		return robotInfo -> robotInfo.getTeam() != team;
	}

	public static Predicate<RobotInfo> robotOpponentTeam(final Team team) {
		final Team opponent = team.opponent();
		return robotInfo -> robotInfo.getTeam() == opponent;
	}

	/**
	 * WARNING:
	 *     This should only ever be used where the argument MapLocations
	 *     are GUARANTEED to not throw a GameActionException.
	 */
	public static Predicate<MapLocation> mapLocOnMapSilenced(final RobotController rc) {
		return UtilBattlecode.silenceGameActionPredicate(mapLoc -> rc.onTheMap(mapLoc));
	}

	/**
	 * WARNING:
	 *     This should only ever be used where the argument MapLocations
	 *     are GUARANTEED to not throw a GameActionException.
	 */
	public static Predicate<MapLocation> mapLocUnoccupiedSilenced(final RobotController rc) {
		return UtilBattlecode.silenceGameActionPredicate(mapLoc -> !rc.isLocationOccupied(mapLoc));
	}

	public static Predicate<MapLocation> mapLocCloser(final MapLocation origin, final MapLocation target) {
		final int originDistSquared = origin.distanceSquaredTo(target);
		return mapLoc -> mapLoc.distanceSquaredTo(target) < originDistSquared;
	}

	public static Predicate<IFlag> flagAssignment() {
		// TODO(theimer): a set of assignment flag types should be maintained in Flag.java.
		return flag ->( (flag instanceof PatrolAssignmentFlag) || (flag instanceof AttackAssignmentFlag) );
	}
}
