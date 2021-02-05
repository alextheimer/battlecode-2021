package player;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import player.handlers.robots.EnlightenmentCenterHandler;
import player.handlers.robots.MuckrakerHandler;
import player.handlers.robots.PoliticianHandler;
import player.handlers.robots.SlandererHandler;
import player.util.battlecode.UtilBattlecode;

/**
 * The Battlecode-required, top-level handler class.
 */
public strictfp class RobotPlayer {

	// Set 'false' for competition code to ignore exceptions and hope for the best...
	private static final boolean RESIGN_ON_EXCEPTION = true;

	/**
	 * Handles a specific RobotType controller.
	 */
	public interface IRobotHandler {
		/**
		 * Handles all movements/communications/actions of a specific robot.
		 *
		 * @param rc the robot's RobotController for the current round.
		 * @return the IRobotHandler to be used during the next round.
		 */
		IRobotHandler handle(RobotController rc);
	}

	/**
	 * Instantiates a handler for a robot.
	 *
	 * @param rc the robot's current RobotController.
	 * @return the robot's handler.
	 */
	private static IRobotHandler getHandler(final RobotController rc) {
		switch (rc.getType()) {
			case ENLIGHTENMENT_CENTER: return new EnlightenmentCenterHandler();
			case POLITICIAN: return new PoliticianHandler(rc);
			case MUCKRAKER: return new MuckrakerHandler();
			case SLANDERER: return new SlandererHandler(rc);
			default: throw new IllegalArgumentException("unrecognized enum value: " + rc.getType());
		}
	}

    /**
     * Handles a RobotController.
     * This is the method called directly by the Battlecode backend.
     */
    public static void run(final RobotController rc) {
    	UtilBattlecode.log("new RobotController: " + rc.getType());
    	IRobotHandler handler = RobotPlayer.getHandler(rc);
    	// if the loop exits, the robot dies.
        while (true) {
            try {
                handler = handler.handle(rc);
                Clock.yield();  // wait until the next turn.
	        } catch (final Exception e) {
	        	// Other RobotControllers are unaffected by any exceptions thrown here.
	        	e.printStackTrace();
	        	if (RobotPlayer.RESIGN_ON_EXCEPTION) {
	        		rc.resign();
	        	}
	        }
        }
    }
}
