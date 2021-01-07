package player;

import java.util.HashMap;
import java.util.Map;

import battlecode.common.*;
import player.handlers.piecetype.*;
import player.handlers.HandlerCommon.*;

public strictfp class RobotPlayer {

	private static Map<RobotType, IRobotHandlerFactory> typeHandlerFactoryMap = new HashMap<>();
	static {
		typeHandlerFactoryMap.put(RobotType.POLITICIAN, new PoliticianHandlerFactory());
		typeHandlerFactoryMap.put(RobotType.ENLIGHTENMENT_CENTER, new EnlightenmentCenterHandlerFactory());
		typeHandlerFactoryMap.put(RobotType.MUCKRAKER, new MuckrakerHandlerFactory());
		typeHandlerFactoryMap.put(RobotType.SLANDERER, new SlandererHandlerFactory());
	}
	
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) {
    	
    	IRobotHandlerFactory typeHandlerFactory = typeHandlerFactoryMap.get(rc.getType());
    	IRobotHandler typeHandler = typeHandlerFactory.instantiate();
        // controller for role

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                typeHandler.handle(rc);
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (GameActionException e) {
                System.out.println(rc.getType() + " GameActionException");
                e.printStackTrace();
            }
        }
    }
}
