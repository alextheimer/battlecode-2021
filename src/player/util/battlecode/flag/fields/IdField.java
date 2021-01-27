package player.util.battlecode.flag.fields;

import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.IFlagFieldFactory;

public class IdField implements IFlagField {
	public static final int NUM_BITS = 15;
	
	private int id;
	
	public IdField(int id) {
		// TODO(theimer): assertions
		this.id = id;
	}
	
	public static IdField fromBits(int bits) {
		// TODO(theimer): !!!!!!!!!!
		return new IdField(bits);
	}
	
	public int value() {
		return this.id;
	}

	@Override
	public int encode() {
		return this.id;
	}

	@Override
	public int numBits() {
		return NUM_BITS;
	}
	
	public static IFlagFieldFactory getFactory() {
		return new IFlagFieldFactory() {

			@Override
			public IFlagField decode(int bits) {
				return IdField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}