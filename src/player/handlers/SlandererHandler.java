package player.handlers;

import battlecode.common.*;
import player.util.Flag;
import player.util.Flag.OpCode;
import player.util.Flag.PatrolAssignmentFlag;
import player.util.UtilMath.DoubleVec2D;
import player.util.UtilMath.Line2D;
import player.util.UtilMath;

import static player.handlers.HandlerCommon.*;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Optional;

public class SlandererHandler implements IRobotHandler {
	
	private LinearMoverHandler moveHandler = null;  // TODO(theimer): get rid of this
	
	public SlandererHandler() {
		
	}
	
	private Optional<PatrolAssignmentFlag> findAssignmentFlag(RobotController rc) throws GameActionException {
		Optional<SimpleImmutableEntry<RobotInfo, Integer>> entry = HandlerCommon.findFirstMatchingTeamFlag(rc, rc.senseNearbyRobots(), 
				(robotInfo, rawFlag) -> Flag.getOpCode(rawFlag) == OpCode.ASSIGN_PATROL);
		return entry.isPresent() ? Optional.of(PatrolAssignmentFlag.decode(entry.get().getValue())) : Optional.empty();
	}
	
	@Override
	public IRobotHandler handle(RobotController rc) throws GameActionException {
		if (moveHandler == null) {
			PatrolAssignmentFlag flag = findAssignmentFlag(rc).get();
			int degrees = flag.getOutboundDegrees();
			DoubleVec2D vec = UtilMath.degreesToVec(degrees);
			DoubleVec2D origin = new DoubleVec2D(rc.getLocation().x, rc.getLocation().y);
			Line2D line = Line2D.make(vec, origin);
			this.moveHandler = new LinearMoverHandler(line, vec);
			
		}
        if (!this.moveHandler.atEndOfLine()) {
        	this.moveHandler.step(rc);
        }
        return this;
	}
}