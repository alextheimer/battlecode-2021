package player.util.battlecode.flag;

import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;

/**
 * Should be imported by the client (along with the `types` package) to encode/decode flags.
 */
public class Flag {

	public static int MAX_NUM_BITS = 24;  // Battlecode-imposed limit per flag
	public static int EMPTY_FLAG = 0;

	public static interface IFlag {
		// TODO(theimer): neither of these methods apply to client code

		/**
		 * Encodes the IFlag into a sequence of bits.
		 * @return the bits of the encoded IFlag.
		 */
		public int encode();
		/**
		 * Returns the number of bits used to encode the IFlag.
		 */
		public int numBits();
	}

	/**
	 * Decodes the bits of an encoded IFlag into an IFlag instance.
	 *
	 * @param rawFlag must lie on [0, 2**MAX_NUM_BITS)
	 * @return an instance of IFlag as described by the IFlag encoded within the argument bits.
	 */
	public static Flag.IFlag decode(final int rawFlag) {
		assert UtilFlag.validBits(Flag.MAX_NUM_BITS, rawFlag) : "rawFlag: " + rawFlag;
		final FlagWalker flagWalker = new FlagWalker(rawFlag);
		// get the FlagOpCode
		final int opCodeBits = flagWalker.readBits(UtilFlag.NUM_OP_CODE_BITS);
		final UtilFlag.FlagOpCode opCode = UtilFlag.getOpCodeFromBits(opCodeBits);
		// use the FlagOpCode to get the correct factory object
		final UtilFlag.IFlagFactory factory = UtilFlag.opCodeToFactoryMap.get(opCode);
		final int flagBits = flagWalker.readBits(factory.numBits());
		return factory.decode(flagBits);
	}

	/**
	 * Returns the IFlag as an encoded sequence of bits.
	 */
	public static int encode(final Flag.IFlag flag) {
		final UtilFlag.FlagOpCode opCode = UtilFlag.flagToOpCodeMap.get(flag.getClass());
		final int opCodeBits = UtilFlag.getBitsFromOpCode(opCode);
		final FlagWalker flagWalker = new FlagWalker(Flag.EMPTY_FLAG);
		// write the FlagOpCode to facilitate later decoding
		flagWalker.writeBits(UtilFlag.NUM_OP_CODE_BITS, opCodeBits);
		// write the bits of the encoded flag
		flagWalker.writeBits(flag.numBits(), flag.encode());
		return flagWalker.getAllBits();
	}
}
