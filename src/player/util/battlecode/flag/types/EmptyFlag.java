package player.util.battlecode.flag.types;

import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;

public class EmptyFlag implements IFlag {

	private static final int NUM_BITS = 1;

	@Override
	public int encode() {
		return 0;  // technically Flag.EMPTY_FLAG contains an op-code
	}

	@Override
	public int numBits() {
		return EmptyFlag.NUM_BITS;
	}

	public static IFlagFactory getFactory() {
		return new IFlagFactory() {

			@Override
			public Flag.IFlag decode(final int bits) {
				assert bits == 0 : "expected no bits, but got: " + bits;
				return new EmptyFlag();
			}

			@Override
			public int numBits() {
				return EmptyFlag.NUM_BITS;
			}

		};
	}

}
