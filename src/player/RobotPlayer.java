package player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import battlecode.common.*;
import player.handlers.piecetype.*;
import player.handlers.roletype.*;
import player.handlers.HandlerCommon.*;

public strictfp class RobotPlayer {
	
	private static Map<RobotType, Supplier<IRobotTypeHandler>> mapThing = new HashMap<>();
	static {
		mapThing.put(RobotType.POLITICIAN, () -> new PoliticianHandler());
		mapThing.put(RobotType.MUCKRAKER, () -> new MuckrakerHandler());
		mapThing.put(RobotType.SLANDERER, () -> new SlandererHandler());
		mapThing.put(RobotType.ENLIGHTENMENT_CENTER, () -> new EnlightenmentCenterHandler());
	}
	
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) {
    	RobotType currentType = rc.getType();
    	RobotRole currentRole = currentType == RobotType.ENLIGHTENMENT_CENTER ? RobotRole.NONE : RobotRole.UNASSIGNED;
    	SquadState state = new SquadState(currentRole, new SquadOrders());
    	
    	IRobotRoleHandler roleHandler;
    	IRobotTypeHandler typeHandler;
    	
    	try {
    		roleHandler = currentType == RobotType.ENLIGHTENMENT_CENTER ? new NoneHandler() : new UnassignedHandlerTODO(rc);
    		typeHandler = mapThing.get(currentType).get();
    	} catch (GameActionException e) {
    		System.out.println(rc.getType() + " GameActionException at instantiation!");
    		return;
    	}
    	
        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
        	System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
        	System.out.println("ROLE:" + state.role);
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                roleHandler = roleHandler.handle(rc);
                typeHandler = typeHandler.handle(rc);
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (GameActionException e) {
                System.out.println(rc.getType() + " GameActionException");
                e.printStackTrace();
            }
        }
    }
}
