package player.util.battlecode.flag.util;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import player.util.battlecode.flag.fields.CoordField;
import player.util.battlecode.flag.fields.RobotTypeField;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.EnemySightedFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;
import player.util.battlecode.flag.types.TargetMissingFlag;
import player.util.math.UtilMath;

/**
 * Contains flag-related utility types/constants/functions.
 */
public class UtilFlag {

	// public to support fast 
	public static enum FlagOpCode { EMPTY, ASSIGN_PATROL, TARGET_MISSING, ENEMY_SIGHTED, ASSIGN_ATTACK }
	public static int EMPTY_FLAG = 0;  // TODO(theimer): get rid of this?
	public static int MAX_NUM_BITS = 24;
	public static FlagOpCode opCodeValues[] = FlagOpCode.values();
	public static int numOpCodeBits = UtilMath.log2Ceil(opCodeValues.length);
	
	public static interface IFlag {
		public int encode();
		public int numBits();
	}
	
	public static interface IFlagFactory {
		public IFlag decode(int bits);
		public int numBits();
	}
	
	public interface IFlagFieldFactory {
		public IFlagField decode(int bits);
		public int numBits();
	}

	// TODO(theimer): move to BaseFlag
	public interface IFlagField {
		public int encode();
		public int numBits();
	}
	
	// TODO(theimer)
	private static List<SimpleImmutableEntry<Class<? extends IFlag>, FlagOpCode>> TODOpairs = Arrays.asList(
			new SimpleImmutableEntry<Class<? extends IFlag>, FlagOpCode>(AttackAssignmentFlag.class, FlagOpCode.ASSIGN_ATTACK),
			new SimpleImmutableEntry<Class<? extends IFlag>, FlagOpCode>(EnemySightedFlag.class, FlagOpCode.ENEMY_SIGHTED),
			new SimpleImmutableEntry<Class<? extends IFlag>, FlagOpCode>(PatrolAssignmentFlag.class, FlagOpCode.ASSIGN_PATROL),
			new SimpleImmutableEntry<Class<? extends IFlag>, FlagOpCode>(TargetMissingFlag.class, FlagOpCode.TARGET_MISSING)
	);
	private static List<SimpleImmutableEntry<FlagOpCode, IFlagFactory>> factoryPairs = Arrays.asList(
			new SimpleImmutableEntry<FlagOpCode, IFlagFactory>(FlagOpCode.ASSIGN_ATTACK, AttackAssignmentFlag.getFactory()),
			new SimpleImmutableEntry<FlagOpCode, IFlagFactory>(FlagOpCode.ENEMY_SIGHTED, EnemySightedFlag.getFactory()),
			new SimpleImmutableEntry<FlagOpCode, IFlagFactory>(FlagOpCode.ASSIGN_PATROL, PatrolAssignmentFlag.getFactory()),
			new SimpleImmutableEntry<FlagOpCode, IFlagFactory>(FlagOpCode.TARGET_MISSING, TargetMissingFlag.getFactory())
	);
	
	private static Map<Class<? extends IFlag>, FlagOpCode> flagToOpCodeMap = new HashMap<>();
	private static Map<FlagOpCode, IFlagFactory> opCodeToFactoryMap = new HashMap<>();
	static {
		for (SimpleImmutableEntry<Class<? extends IFlag>, FlagOpCode> entry : TODOpairs) {
			flagToOpCodeMap.put(entry.getKey(), entry.getValue());
		}
		for (SimpleImmutableEntry<FlagOpCode, IFlagFactory> entry : factoryPairs) {
			opCodeToFactoryMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	public static FlagOpCode getOpCodeFromBits(int bits) {
		return opCodeValues[bits];
	}
	
	public static int getBitsFromOpCode(FlagOpCode opCode) {
		return opCode.ordinal();
	}
	
	public static IFlag decode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		int opCodeBits = flagWalker.readBits(numOpCodeBits);
		FlagOpCode opCode = getOpCodeFromBits(opCodeBits);
		IFlagFactory factory = opCodeToFactoryMap.get(opCode);
		int flagBits = flagWalker.readBits(factory.numBits());
		return factory.decode(flagBits);
	}
	
	public static int encode(IFlag flag) {
		FlagOpCode opCode = flagToOpCodeMap.get(flag.getClass());
		int opCodeBits = getBitsFromOpCode(opCode);
		FlagWalker flagWalker = new FlagWalker(0);
		flagWalker.writeBits(numOpCodeBits, opCodeBits);
		flagWalker.writeBits(flag.numBits(), flag.encode());
		return flagWalker.getAllBits();
	}
}
