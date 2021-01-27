package player.util.battlecode.flag.util;

import player.util.math.UtilMath;

public class UtilFlag {

	public static enum OpCode {EMPTY, ASSIGN_PATROL, TARGET_MISSING, FOLLOWER_CLAIM, ENEMY_SIGHTED, ASSIGN_ATTACK, BASE_SIGHTED}

	public static interface IFlag {
		public OpCode getOpCode();
		public int encode();
	}
	public static int EMPTY_FLAG = 0;
	public static int NUM_BITS = 24;
	public static OpCode opCodeValues[] = OpCode.values();
	public static int numOpCodeBits = UtilMath.log2Ceil(opCodeValues.length);
	public static OpCode getOpCode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		int opCodeID = flagWalker.readBits(numOpCodeBits);
		return opCodeValues[opCodeID];
	}

}
