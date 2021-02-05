package player.util.battlecode.flag.types.base.fields;

import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.math.UtilMath;

public class DegreesField implements BaseFlag.IFlagField {
	
	// log2ceil(360) = 9
	public static final int NUM_BITS = 9;
	
	private int degrees;
	
	/**
	 * Stores a value on [0, 360).
	 * 
	 * @param degrees must lie on [0, 360).
	 */
	public DegreesField(int degrees) {
		assert (degrees >= 0) && (degrees < UtilMath.CIRCLE_DEGREES) : "degrees: " + degrees;
		this.degrees = degrees;
	}
	
	public int getDegrees() {
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
				assert UtilFlag.validBits(NUM_BITS, bits) : "" + bits;
				return new DegreesField(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}