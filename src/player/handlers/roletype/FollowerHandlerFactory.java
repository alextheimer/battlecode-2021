package player.handlers.roletype;

import battlecode.common.*;
import static player.handlers.HandlerCommon.*;

class FollowerHandler implements IRobotHandler {

	public FollowerHandler() {
		// blank
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
		return;
	}
	
}

public class FollowerHandlerFactory implements IRobotHandlerFactory {
	
	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new FollowerHandler();
	}

}
