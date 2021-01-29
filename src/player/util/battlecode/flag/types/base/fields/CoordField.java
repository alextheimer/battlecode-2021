package player.util.battlecode.flag.types.base.fields;

import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.math.IntVec2D;

public class CoordField implements BaseFlag.IFlagField {
	public static final int NUM_BITS = 14;
	private static final int NUM_BITS_PER = 7;
	
	private int x;
	private int y;
	
	public CoordField(int x, int y) {
		// TODO(theimer): assertions
		this.x = x;
		this.y = y;
	}

	// TODO(theimer): rename all of these to decode() for consistency's sake
	public static CoordField fromBits(int bits) {
		FlagWalker flagWalker = new FlagWalker(bits);
		int x = flagWalker.readBits(NUM_BITS_PER);
		int y = flagWalker.readBits(NUM_BITS_PER);
		return new CoordField(x, y);
	}
	
	public IntVec2D value() {
		return new IntVec2D(this.x, this.y);
	}

	@Override
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(0);
		flagWalker.writeBits(NUM_BITS_PER, this.x);
		flagWalker.writeBits(NUM_BITS_PER, this.y);
		return flagWalker.getAllBits();
	}

	@Override
	public int numBits() {
		return NUM_BITS;
	}
	
	public static BaseFlag.IFlagFieldFactory getFactory() {
		return new BaseFlag.IFlagFieldFactory() {

			@Override
			public BaseFlag.IFlagField decode(int bits) {
				return CoordField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}