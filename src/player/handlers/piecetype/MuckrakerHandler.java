package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon;

public class MuckrakerHandler {
	public static void handle(RobotController rc, HandlerCommon.RobotState state) throws GameActionException {
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
        if (HandlerCommon.tryMove(rc, HandlerCommon.randomDirection()))
            System.out.println("I moved!");
	}
}
