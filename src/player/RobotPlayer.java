package player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import battlecode.common.*;
import player.handlers.*;
import player.handlers.EnlightenmentCenterHandler;
import player.handlers.MuckrakerHandler;
import player.handlers.PoliticianHandler;
import player.handlers.SlandererHandler;
import player.handlers.HandlerCommon.*;

public strictfp class RobotPlayer {
	
	private static Map<RobotType, Supplier<IRobotHandler>> mapThing = new HashMap<>();
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
    	
    	while (rc.getTeam() == Team.A) {
    		Clock.yield();
    	}
    	
    	RobotType currentType = rc.getType();
    	
    	IRobotHandler typeHandler;
    	
//    	try {
    		typeHandler = mapThing.get(currentType).get();
//    	} catch (GameActionException e) {
//    		System.out.println(rc.getType() + " GameActionException at instantiation!");
//    		return;
//    	}
    	
        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
        	System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                typeHandler = typeHandler.handle(rc);
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " GameActionException");
                e.printStackTrace();
            } catch (AssertionError e) {
	        	e.printStackTrace();
	        	rc.resign();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        	rc.resign();
	        }
        }
    }
}
