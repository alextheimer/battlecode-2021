package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.IRobotHandlerFactory;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class SlandererHandler implements IRobotHandler {
	
	public SlandererHandler() {
		// blank
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
        if (tryMove(rc, randomDirection()))
            System.out.println("I moved!");
	}
}

public class SlandererHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new SlandererHandler();
	}

}