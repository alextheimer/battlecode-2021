package player.handlers.robots;

import static player.handlers.common.HandlerCommon.*;

import battlecode.common.*;
import player.handlers.common.HandlerCommon;
import player.handlers.common.HandlerCommon.IRobotHandler;

public class MuckrakerHandler implements IRobotHandler {
	
	public MuckrakerHandler() {
		//blank
	}
	
	@Override
	public IRobotHandler handle(RobotController rc) throws GameActionException {
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
        if(attemptMove(rc, randomDirection()))
            System.out.println("I moved!");
        return this;
	}


}
