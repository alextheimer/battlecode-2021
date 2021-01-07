package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class NoneHandler implements IRobotHandler {

	public NoneHandler() {
		// blank
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
		return;
	}

}

public class NoneHandlerFactory implements IRobotHandlerFactory {
	
	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new NoneHandler();
	}

}
