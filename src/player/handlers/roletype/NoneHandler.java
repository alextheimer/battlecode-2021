package player.handlers.roletype;

import battlecode.common.*;
import player.handlers.HandlerCommon.SquadState;

import static player.handlers.HandlerCommon.*;

public class NoneHandler implements IRobotRoleHandler {

	public NoneHandler() {
		// blank
	}
	
	@Override
	public IRobotRoleHandler handle(RobotController rc) throws GameActionException {
		return this;
	}

}