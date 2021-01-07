package player;

import java.util.HashMap;
import java.util.Map;

import battlecode.common.*;
import player.handlers.piecetype.*;
import player.handlers.roletype.*;
import player.handlers.HandlerCommon.*;

public strictfp class RobotPlayer {

	private static Map<RobotRole, IRobotHandlerFactory> roleHandlerFactoryMap = new HashMap<>();
	private static Map<RobotType, IRobotHandlerFactory> typeHandlerFactoryMap = new HashMap<>();
	static {
		// Role handlers ---------------------------------------------------------------------
		roleHandlerFactoryMap.put(RobotRole.UNASSIGNED, new UnassignedHandlerFactory());
		roleHandlerFactoryMap.put(RobotRole.LEADER, new LeaderHandlerFactory());
		roleHandlerFactoryMap.put(RobotRole.FOLLOWER, new FollowerHandlerFactory());
		roleHandlerFactoryMap.put(RobotRole.NONE, new NoneHandlerFactory());
		// Type handlers ---------------------------------------------------------------------
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
    	RobotType currentType = rc.getType();
    	RobotRole currentRole = currentType == RobotType.ENLIGHTENMENT_CENTER ? RobotRole.NONE : RobotRole.UNASSIGNED;
    	RobotState state = new RobotState(currentRole);
    	
    	IRobotHandler roleHandler = roleHandlerFactoryMap.get(currentRole).instantiate(rc, state);
    	IRobotHandler typeHandler = typeHandlerFactoryMap.get(currentType).instantiate(rc, state);
    	
        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
        	System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                roleHandler.handle(rc, state);
                typeHandler.handle(rc, state);
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (GameActionException e) {
                System.out.println(rc.getType() + " GameActionException");
                e.printStackTrace();
            }
            
            if (currentType != rc.getType()) {
            	currentType = rc.getType();
            	typeHandler = typeHandlerFactoryMap.get(currentType).instantiate(rc, state);
            }
            if (currentRole != state.role) {
            	currentRole = state.role;
            	roleHandler = roleHandlerFactoryMap.get(currentRole).instantiate(rc, state);
            }
        }
    }
}
