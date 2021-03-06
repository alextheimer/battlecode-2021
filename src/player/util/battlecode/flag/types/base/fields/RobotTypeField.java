package player.util.battlecode.flag.types.base.fields;

import battlecode.common.RobotType;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.math.UtilMath;

public class RobotTypeField implements BaseFlag.IFlagField {

	// number of bits needed to represent all RobotType.values().length values
	public static final int NUM_BITS = UtilMath.log2Ceil(RobotType.values().length);
	private static final RobotType[] ROBOT_TYPE_ARRAY = RobotType.values();

	private final RobotType robotType;

	/**
	 * Stores/encodes/decodes a RobotType.
	 */
	public RobotTypeField(final RobotType robotType) {
		this.robotType = robotType;
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
		return RobotTypeField.NUM_BITS;
	}

	public static BaseFlag.IFlagFieldFactory getFactory() {
		return new BaseFlag.IFlagFieldFactory() {

			@Override
			public BaseFlag.IFlagField decode(final int bits) {
				assert UtilFlag.validBits(RobotTypeField.NUM_BITS, bits) : "" + bits;
				return new RobotTypeField(RobotTypeField.ROBOT_TYPE_ARRAY[bits]);
			}

			@Override
			public int numBits() {
				return RobotTypeField.NUM_BITS;
			}

		};
	}
}