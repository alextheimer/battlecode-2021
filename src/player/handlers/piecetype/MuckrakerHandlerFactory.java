package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.IRobotHandlerFactory;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class MuckrakerState extends RobotState {
	// TODO(theimer)
}

class MuckrakerHandler implements IRobotHandler {
	private MuckrakerState state;
	
	public MuckrakerHandler() {
		this.state = new MuckrakerState();
	}
	
	@Override
	public void handle(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (tryMove(rc, randomDirection()))
            System.out.println("I moved!");
	}


}

public class MuckrakerHandlerFactory implements IRobotHandlerFactory {
	@Override
	public IRobotHandler instantiate() {
		return new MuckrakerHandler();
	}

	@Override
	public IRobotHandler instantiateFromState(RobotState state) {
		return new MuckrakerHandler();
	}

}
