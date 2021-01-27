package player.util.battlecode.flag.fields;

import battlecode.common.RobotType;
import player.util.math.UtilMath;

public class RobotTypeField {
	public static final int NUM_BITS = UtilMath.log2Ceil(RobotType.values().length);
	
	private RobotType robotType;
	
	public RobotTypeField(RobotType robotType) {
		// TODO(theimer): assertions
		this.robotType = robotType;
	}
	
	public int toBits() {
		return this.robotType.ordinal();
	}
	
	public static RobotTypeField fromBits(int bits) {
		// TODO(theimer): !!!!!!!!!!
		return new RobotTypeField(RobotType.values()[bits]);
	}
	
	public RobotType value() {
		return this.robotType;
	}
}