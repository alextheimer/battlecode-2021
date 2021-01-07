package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.IRobotHandlerFactory;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class SlandererState extends RobotState {
	// TODO(theimer)
}

class SlandererHandler implements IRobotHandler {
	private SlandererState state;
	
	public SlandererHandler() {
		this.state = new SlandererState();
	}
	
	@Override
	public void handle(RobotController rc) throws GameActionException {
        if (tryMove(rc, randomDirection()))
            System.out.println("I moved!");
	}
}

public class SlandererHandlerFactory implements IRobotHandlerFactory {
	@Override
	public IRobotHandler instantiate() {
		return new SlandererHandler();
	}

	@Override
	public IRobotHandler instantiateFromState(RobotState state) {
		return new SlandererHandler();
	}

}