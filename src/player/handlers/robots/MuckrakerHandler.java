package player.handlers.robots;

import static player.handlers.common.HandlerCommon.*;

import battlecode.common.*;
import player.RobotPlayer;
import player.RobotPlayer.IRobotHandler;
import player.handlers.common.HandlerCommon;

public class MuckrakerHandler implements RobotPlayer.IRobotHandler {
	
	public MuckrakerHandler() {
		//blank
	}
	
	@Override
	public RobotPlayer.IRobotHandler handle(RobotController rc) throws GameActionException {
//        Team enemy = rc.getTeam().opponent();
//        int actionRadius = rc.getType().actionRadiusSquared;
//        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
//            if (robot.type.canBeExposed()) {
//                // It's a slanderer... go get them!
//                if (rc.canExpose(robot.location)) {
//                    UtilBattlecode.log("e x p o s e d");
//                    rc.expose(robot.location);
//                    return this;
//                }
//            }
//        }
//        if(attemptMove(rc, randomDirection()))
//            UtilBattlecode.log("I moved!");
        return this;
	}


}
