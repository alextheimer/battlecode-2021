package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.IRobotHandlerFactory;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class MuckrakerHandler implements IRobotHandler {
	
	public MuckrakerHandler() {
		//blank
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
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
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new MuckrakerHandler();
	}
}
