package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon;

public class SlandererHandler {
	public static void handle(RobotController rc, HandlerCommon.RobotState state) throws GameActionException {
        if (HandlerCommon.tryMove(rc, HandlerCommon.randomDirection()))
            System.out.println("I moved!");
	}
}
