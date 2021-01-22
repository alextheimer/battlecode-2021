package player.handlers;

import battlecode.common.*;

import static player.handlers.HandlerCommon.*;

public class SlandererHandler implements IRobotHandler {
	
	public SlandererHandler() {
		// blank
	}
	
	@Override
	public IRobotHandler handle(RobotController rc) throws GameActionException {
        if (attemptMove(rc, randomDirection()))
            System.out.println("I moved!");
        return this;
	}
}