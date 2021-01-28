package player.util.battlecode.flag.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import player.util.battlecode.flag.fields.CoordField;
import player.util.battlecode.flag.fields.RobotTypeField;
import player.util.battlecode.flag.types.EnemySightedFlag;
import player.util.math.UtilMath;


public class UtilFlag {

	public static enum OpCode { EMPTY, ASSIGN_PATROL, TARGET_MISSING, ENEMY_SIGHTED, ASSIGN_ATTACK }
	public static int EMPTY_FLAG = 0;
	public static int NUM_BITS = 24;
	public static OpCode opCodeValues[] = OpCode.values();
	public static int numOpCodeBits = UtilMath.log2Ceil(opCodeValues.length);
	
	
	public static interface IFlag {
		public int encode();
	}
	
	public interface IFlagFieldFactory {
		public IFlagField decode(int bits);
		public int numBits();
	}

	public interface IFlagField {
		public int encode();
		public int numBits();
	}
	
	public static OpCode getOpCode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		int opCodeID = flagWalker.readBits(numOpCodeBits);
		return opCodeValues[opCodeID];
	}

}
