package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

public class NoneHandler implements IRobotRoleHandler {

	public NoneHandler() {
		// blank
	}
	
	@Override
	public IRobotRoleHandler handle(RobotController rc, RobotState state) throws GameActionException {
		return this;
	}

}