package player.util.battlecode.flag.types.base.fields;

import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;

public class IdField implements BaseFlag.IFlagField {
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
	
	public static BaseFlag.IFlagFieldFactory getFactory() {
		return new BaseFlag.IFlagFieldFactory() {

			@Override
			public BaseFlag.IFlagField decode(int bits) {
				return IdField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}