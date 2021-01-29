package player.util.battlecode.flag.types.base.fields;

import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;

public class DegreesField implements BaseFlag.IFlagField {
	public static final int NUM_BITS = 9;
	
	private int degrees;
	
	public DegreesField(int degrees) {
		this.degrees = degrees;
	}
	
	public static DegreesField fromBits(int bits) {
		return new DegreesField(bits);
	}
	
	public int value() {
		return this.degrees;
	}

	@Override
	public int encode() {
		return this.degrees;
	}

	@Override
	public int numBits() {
		return NUM_BITS;
	}
	
	public static BaseFlag.IFlagFieldFactory getFactory() {
		return new BaseFlag.IFlagFieldFactory() {

			@Override
			public BaseFlag.IFlagField decode(int bits) {
				return DegreesField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}