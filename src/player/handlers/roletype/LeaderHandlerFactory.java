package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;
import util.search.BFSGenerator;
import util.Flag;

import util.Util;
import util.UtilMath.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static player.handlers.HandlerCommon.*;

class LeaderHandler implements IRobotHandler {
	
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
	
	private void handlePatrol(RobotController rc, RobotState state) {
		
	}
	
	private void handleOccupy(RobotController rc, RobotState state) {
		
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
		// TODO(theimer): remove these checks?
		if (this.claimCooldown > 0) {
			this.claimCooldown--;
			return;
		}
		if (this.claimCooldown == 0) {
			this.claimCooldown--;
			rc.setFlag(Flag.EMPTY_FLAG);
		}
		
		switch(state.orders.squadType) {
		case PATROL:
//			handlePatrol(RobotController rc, RobotState state);
			
			if (rc.canMove(Direction.NORTH)) {
				rc.move(Direction.NORTH);
			}
			break;
		case OCCUPY:
//			handleOccupy(RobotController rc, RobotState state);
			break;
		case NONE:
		case UNASSIGNED:
			assert false : "leaders shouldn't have these SquadTypes!";
			break;
		}
	}

}

public class LeaderHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new LeaderHandler();
	}

}
