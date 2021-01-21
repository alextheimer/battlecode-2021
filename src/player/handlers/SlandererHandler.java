package player.handlers;

import battlecode.common.*;

import static player.handlers.HandlerCommon.*;

public class SlandererHandler implements IRobotTypeHandler {
	
	public SlandererHandler() {
		// blank
	}
	
	@Override
	public IRobotTypeHandler handle(RobotController rc) throws GameActionException {
        if (attemptMove(rc, randomDirection()))
            System.out.println("I moved!");
        return this;
	}
}