package player.util.battlecode.flag.fields;

import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.IFlagFieldFactory;
import player.util.math.IntVec2D;

public class DiffVecField implements IFlagField {
	public static final int NUM_BITS = 8;
	
	private static final int numBitsPerDim = 4;
	private static final int offset = 6;
	
	private IntVec2D vec;
	
	public DiffVecField(int x, int y) {
		// TODO(theimer): assertions
		this.vec = new IntVec2D(x, y);
	}
	
	public static DiffVecField fromBits(int bits) {
		FlagWalker flagWalker = new FlagWalker(bits);
		int x = flagWalker.readBits(numBitsPerDim) - offset;
		int y = flagWalker.readBits(numBitsPerDim) - offset;
		return new DiffVecField(x, y);
	}
	
	public IntVec2D value() {
		return this.vec;
	}

	@Override
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(0);
		flagWalker.writeBits(numBitsPerDim, this.vec.x + offset);
		flagWalker.writeBits(numBitsPerDim, this.vec.y + offset);
		return flagWalker.getAllBits();
	}

	@Override
	public int numBits() {
		return NUM_BITS;
	}
	
	public static IFlagFieldFactory getFactory() {
		return new IFlagFieldFactory() {

			@Override
			public IFlagField decode(int bits) {
				return DiffVecField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}