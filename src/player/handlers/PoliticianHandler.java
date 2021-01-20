package player.handlers;

import battlecode.common.*;

import static player.handlers.HandlerCommon.*;

public class PoliticianHandler implements IRobotTypeHandler {
	
	public PoliticianHandler() {
		// blank
	}
	
	@Override
	public IRobotTypeHandler handle(RobotController rc) throws GameActionException {
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
		return this;
	}
}
