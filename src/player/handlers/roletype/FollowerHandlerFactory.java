package player.handlers.roletype;

import battlecode.common.*;
import static player.handlers.HandlerCommon.*;

class FollowerHandler implements IRobotHandler {

	@Override
	public void handle(RobotController rc) throws GameActionException {
		return;
	}
	
}

public class FollowerHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate() {
		return new FollowerHandler();
	}

	@Override
	public IRobotHandler instantiateFromState(RobotState state) {
		return new FollowerHandler();
	}

}
