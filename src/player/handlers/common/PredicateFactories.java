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

public class PredicateFactories {
	// max distance squared between two adjacent MapLocations
	private final static int MAX_ADJACENT_DIST_SQUARED = 2;
	
	public static Predicate<RobotInfo> robotAdjacentTo(MapLocation adjacentTo) {
		return robotInfo -> robotInfo.getLocation()
				.isWithinDistanceSquared(adjacentTo, MAX_ADJACENT_DIST_SQUARED);
	}
	
	public static Predicate<RobotInfo> robotSameTeam(Team team) {
		return robotInfo -> robotInfo.getTeam() == team;
	}
	
	public static Predicate<RobotInfo> robotNonTeam(Team team) {
		return robotInfo -> robotInfo.getTeam() != team;
	}
	
	public static Predicate<RobotInfo> robotOpponentTeam(Team team) {
		Team opponent = team.opponent();
		return robotInfo -> robotInfo.getTeam() == opponent;
	}
	
	public static Predicate<MapLocation> mapLocOnMap(RobotController rc) {
		return UtilBattlecode.silenceGameActionPredicate(mapLoc -> rc.onTheMap(mapLoc));
	}
	
	public static Predicate<MapLocation> mapLocUnoccupied(RobotController rc) {
		return UtilBattlecode.silenceGameActionPredicate(mapLoc -> !rc.isLocationOccupied(mapLoc));
	}
	
	public static Predicate<IFlag> flagAssignment() {
		return flag ->( (flag instanceof PatrolAssignmentFlag) || (flag instanceof AttackAssignmentFlag) );
	}
}
