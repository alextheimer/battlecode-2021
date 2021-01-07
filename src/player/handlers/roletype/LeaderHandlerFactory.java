package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class LeaderHandler implements IRobotHandler {

	@Override
	public void handle(RobotController rc) throws GameActionException {
		return;
	}

}

public class LeaderHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate() {
		return new LeaderHandler();
	}

	@Override
	public IRobotHandler instantiateFromState(RobotState state) {
		return new LeaderHandler();
	}

}
