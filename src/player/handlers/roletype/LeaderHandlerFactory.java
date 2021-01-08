package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class LeaderHandler implements IRobotHandler {
	
	public LeaderHandler() {
		// intentionally blank
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
