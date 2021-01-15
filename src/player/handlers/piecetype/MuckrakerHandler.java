package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

public class MuckrakerHandler implements IRobotTypeHandler {
	
	public MuckrakerHandler(RobotController rc, RobotState state) {
		//blank
	}
	
	@Override
	public IRobotTypeHandler handle(RobotController rc, RobotState state) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return this;
                }
            }
        }
        if (tryMove(rc, randomDirection()))
            System.out.println("I moved!");
        return this;
	}


}
