package player.util.battlecode.flag.fields;

import battlecode.common.RobotType;
import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.IFlagFieldFactory;
import player.util.math.UtilMath;

public class RobotTypeField implements IFlagField {
	
	public static final int NUM_BITS = UtilMath.log2Ceil(RobotType.values().length);
	
	private RobotType robotType;
	
	public RobotTypeField(RobotType robotType) {
		// TODO(theimer): assertions
		this.robotType = robotType;
	}
	
	public static RobotTypeField fromBits(int bits) {
		// TODO(theimer): !!!!!!!!!!
		return new RobotTypeField(RobotType.values()[bits]);
	}
	
	public RobotType value() {
		return this.robotType;
	}

	@Override
	public int encode() {
		return this.robotType.ordinal();
	}

	@Override
	public int numBits() {
		return NUM_BITS;
	}
	
	public static IFlagFieldFactory getFactory() {
		return new IFlagFieldFactory() {

			@Override
			public IFlagField decode(int bits) {
				return RobotTypeField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}