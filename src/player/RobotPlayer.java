package player;

import battlecode.common.*;
import player.handlers.piecetype.*;
import player.handlers.HandlerCommon.*;

public strictfp class RobotPlayer {

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) {
  
    	// TODO(theimer): add state initializers?
        RobotState state = new RobotState();

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: EnlightenmentCenterHandler.handle(rc, state); break;
                    case POLITICIAN:           PoliticianHandler.handle(rc, state);          break;
                    case SLANDERER:            SlandererHandler.handle(rc, state);           break;
                    case MUCKRAKER:            MuckrakerHandler.handle(rc, state);           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (GameActionException e) {
                System.out.println(rc.getType() + " GameActionException");
                e.printStackTrace();
            }
        }
    }
}
