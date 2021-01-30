package player.util.battlecode.flag.types.base.fields;

import battlecode.common.RobotType;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.math.UtilMath;

public class RobotTypeField implements BaseFlag.IFlagField {
	
	// number of bits needed to represent all RobotType.values().length values
	public static final int NUM_BITS = UtilMath.log2Ceil(RobotType.values().length);
	private static final RobotType[] ROBOT_TYPE_ARRAY = RobotType.values();
	
	private RobotType robotType;
	
	/**
	 * Stores/encodes/decodes a RobotType.
	 */
	public RobotTypeField(RobotType robotType) {
		this.robotType = robotType;
	}
	
	public static RobotTypeField decode(int bits) {
		assert (bits >= 0) && (bits < ROBOT_TYPE_ARRAY.length) : "bits: " + bits;
		return new RobotTypeField(ROBOT_TYPE_ARRAY[bits]);
	}
	
	public RobotType getRobotType() {
		return this.robotType;
	}

	@Override
	public int encode() {
		// TODO(theimer): worth changing this?
		return this.robotType.ordinal();
	}

	@Override
	public int numBits() {
		return NUM_BITS;
	}
	
	public static BaseFlag.IFlagFieldFactory getFactory() {
		return new BaseFlag.IFlagFieldFactory() {

			@Override
			public BaseFlag.IFlagField decode(int bits) {
				return RobotTypeField.decode(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}