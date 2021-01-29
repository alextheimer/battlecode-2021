package player.util.battlecode.flag.types.base.fields;

import battlecode.common.RobotType;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.math.UtilMath;

public class RobotTypeField implements BaseFlag.IFlagField {
	
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
	
	public static BaseFlag.IFlagFieldFactory getFactory() {
		return new BaseFlag.IFlagFieldFactory() {

			@Override
			public BaseFlag.IFlagField decode(int bits) {
				return RobotTypeField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}