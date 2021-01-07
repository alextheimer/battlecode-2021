package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class UnassignedHandler implements IRobotHandler {

	@Override
	public void handle(RobotController rc) throws GameActionException {
		return;
	}

}

public class UnassignedHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate() {
		return new UnassignedHandler();
	}

	@Override
	public IRobotHandler instantiateFromState(RobotState state) {
		return new UnassignedHandler();
	}

}
