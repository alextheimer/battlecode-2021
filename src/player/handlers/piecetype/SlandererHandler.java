package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

public class SlandererHandler implements IRobotTypeHandler {
	
	public SlandererHandler(RobotController rc, RobotState state) {
		// blank
	}
	
	@Override
	public IRobotTypeHandler handle(RobotController rc, RobotState state) throws GameActionException {
        if (tryMove(rc, randomDirection()))
            System.out.println("I moved!");
        return this;
	}
}