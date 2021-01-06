package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon;

public class PoliticianHandler {
	public static void handle(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
        if (HandlerCommon.tryMove(rc, HandlerCommon.randomDirection()))
            System.out.println("I moved!");
	}
}
