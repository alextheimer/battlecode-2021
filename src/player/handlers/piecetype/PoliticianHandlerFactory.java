package player.handlers.piecetype;

import battlecode.common.*;
import player.handlers.HandlerCommon.IRobotHandler;
import player.handlers.HandlerCommon.RobotState;

import static player.handlers.HandlerCommon.*;

class PoliticianHandler implements IRobotHandler {
	
	public PoliticianHandler() {
		// blank
	}
	
	@Override
	public void handle(RobotController rc, RobotState state) throws GameActionException {
//        Team enemy = rc.getTeam().opponent();
//        int actionRadius = rc.getType().actionRadiusSquared;
//        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
//        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
//            System.out.println("empowering...");
//            rc.empower(actionRadius);
//            System.out.println("empowered");
//            return;
//        }
//        if (tryMove(rc, randomDirection()))
//            System.out.println("I moved!");
	}
}

public class PoliticianHandlerFactory implements IRobotHandlerFactory {

	@Override
	public IRobotHandler instantiate(RobotController rc, RobotState state) {
		return new PoliticianHandler();
	}

}
