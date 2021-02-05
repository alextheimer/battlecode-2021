package player.handlers.robots;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import player.RobotPlayer;

public class MuckrakerHandler implements RobotPlayer.IRobotHandler {

	@Override
	public RobotPlayer.IRobotHandler handle(final RobotController rc) {
		assert rc.getType() == RobotType.MUCKRAKER : "illegal controller RobotType: " + rc.getType();
		// intentionally blank (unit unused)
        return this;
	}
}
