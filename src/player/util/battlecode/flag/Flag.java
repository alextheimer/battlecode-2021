package player.util.battlecode.flag;

import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;

public class Flag {

	public static int MAX_NUM_BITS = 24;
	public static int EMPTY_FLAG = 0;  // TODO(theimer): get rid of this?
	
	public static interface IFlag {
		public int encode();
		public int numBits();
	}
	
	public static Flag.IFlag decode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		int opCodeBits = flagWalker.readBits(UtilFlag.numOpCodeBits);
		UtilFlag.FlagOpCode opCode = UtilFlag.getOpCodeFromBits(opCodeBits);
		UtilFlag.IFlagFactory factory = UtilFlag.opCodeToFactoryMap.get(opCode);
		int flagBits = flagWalker.readBits(factory.numBits());
		return factory.decode(flagBits);
	}

	public static int encode(Flag.IFlag flag) {
		UtilFlag.FlagOpCode opCode = UtilFlag.flagToOpCodeMap.get(flag.getClass());
		int opCodeBits = UtilFlag.getBitsFromOpCode(opCode);
		FlagWalker flagWalker = new FlagWalker(0);
		flagWalker.writeBits(UtilFlag.numOpCodeBits, opCodeBits);
		flagWalker.writeBits(flag.numBits(), flag.encode());
		return flagWalker.getAllBits();
	}
}
