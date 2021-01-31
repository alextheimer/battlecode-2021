package player.util.battlecode.flag;

import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;

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
	public static Flag.IFlag decode(int rawFlag) {
		assert UtilFlag.validBits(MAX_NUM_BITS, rawFlag) : "rawFlag: " + rawFlag;
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		// get the FlagOpCode
		int opCodeBits = flagWalker.readBits(UtilFlag.NUM_OP_CODE_BITS);
		UtilFlag.FlagOpCode opCode = UtilFlag.getOpCodeFromBits(opCodeBits);
		// use the FlagOpCode to get the correct factory object
		UtilFlag.IFlagFactory factory = UtilFlag.opCodeToFactoryMap.get(opCode);
		int flagBits = flagWalker.readBits(factory.numBits());
		return factory.decode(flagBits);
	}

	/**
	 * Returns the IFlag as an encoded sequence of bits.
	 */
	public static int encode(Flag.IFlag flag) {
		UtilFlag.FlagOpCode opCode = UtilFlag.flagToOpCodeMap.get(flag.getClass());
		int opCodeBits = UtilFlag.getBitsFromOpCode(opCode);
		FlagWalker flagWalker = new FlagWalker(EMPTY_FLAG);
		// write the FlagOpCode to facilitate later decoding
		flagWalker.writeBits(UtilFlag.NUM_OP_CODE_BITS, opCodeBits);
		// write the bits of the encoded flag
		flagWalker.writeBits(flag.numBits(), flag.encode());
		return flagWalker.getAllBits();
	}
}
