package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;
import util.search.BFSGenerator;

import static util.Util.IntVec2D;
import util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static player.handlers.HandlerCommon.*;

class LeaderHandler implements IRobotHandler {
	
	public LeaderHandler() {
		// intentionally blank
	}
	
	private static Set<IntVec2D> expandCoord(IntVec2D coord, RobotController rc) {
		return Util.makeAllAdjacentStream(coord).filter(
				expCoord ->
					((expCoord.x != coord.x) ^ (expCoord.y != coord.y)) &&  // only cardinal
					(rc.canSenseLocation(new MapLocation(coord.x, coord.y)))  // sensable location
				).collect(Collectors.toSet());
	}
	
	private Map<IntVec2D, Double> scanPassabilities(RobotController rc) {
		MapLocation currLoc = rc.getLocation();
		Map<IntVec2D, Double> passabilityMap = new HashMap<>();
		BFSGenerator<IntVec2D> bfsGen = new BFSGenerator<>(new IntVec2D(currLoc.x, currLoc.y), coord -> expandCoord(coord, rc));
		for (IntVec2D vec = bfsGen.next(); vec != null; vec = bfsGen.next()) {
			try {
				passabilityMap.put(vec, rc.sensePassability(new MapLocation(vec.x, vec.y)));
			} catch (GameActionException e) {
				assert false : "expandCoord should prevent this!";
			}
		}
		return passabilityMap;
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
		return;
	}

}

public class LeaderHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new LeaderHandler();
	}

}
