package player.util.battlecode.flag.fields;

import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.IFlagFieldFactory;

public class DegreesField implements IFlagField {
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
	
	public static IFlagFieldFactory getFactory() {
		return new IFlagFieldFactory() {

			@Override
			public IFlagField decode(int bits) {
				return DegreesField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}