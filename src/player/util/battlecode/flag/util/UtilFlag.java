package player.util.battlecode.flag.util;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.EnemySightedFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;
import player.util.battlecode.flag.types.TargetMissingFlag;
import player.util.battlecode.flag.types.base.fields.MapLocField;
import player.util.battlecode.flag.types.base.fields.RobotTypeField;
import player.util.math.UtilMath;

/**
 * Contains flag-related utility types/constants/functions.
 */
public class UtilFlag {

	// public to support fast 
	public static enum FlagOpCode { EMPTY, ASSIGN_PATROL, TARGET_MISSING, ENEMY_SIGHTED, ASSIGN_ATTACK }
	public static FlagOpCode opCodeValues[] = FlagOpCode.values();
	public static int numOpCodeBits = UtilMath.log2Ceil(opCodeValues.length);
	
	public static interface IFlagFactory {
		public Flag.IFlag decode(int bits);
		public int numBits();
	}
	
	// TODO(theimer)
	private static List<SimpleImmutableEntry<Class<? extends Flag.IFlag>, FlagOpCode>> TODOpairs = Arrays.asList(
			new SimpleImmutableEntry<Class<? extends Flag.IFlag>, FlagOpCode>(AttackAssignmentFlag.class, FlagOpCode.ASSIGN_ATTACK),
			new SimpleImmutableEntry<Class<? extends Flag.IFlag>, FlagOpCode>(EnemySightedFlag.class, FlagOpCode.ENEMY_SIGHTED),
			new SimpleImmutableEntry<Class<? extends Flag.IFlag>, FlagOpCode>(PatrolAssignmentFlag.class, FlagOpCode.ASSIGN_PATROL),
			new SimpleImmutableEntry<Class<? extends Flag.IFlag>, FlagOpCode>(TargetMissingFlag.class, FlagOpCode.TARGET_MISSING)
	);
	private static List<SimpleImmutableEntry<FlagOpCode, IFlagFactory>> factoryPairs = Arrays.asList(
			new SimpleImmutableEntry<FlagOpCode, IFlagFactory>(FlagOpCode.ASSIGN_ATTACK, AttackAssignmentFlag.getFactory()),
			new SimpleImmutableEntry<FlagOpCode, IFlagFactory>(FlagOpCode.ENEMY_SIGHTED, EnemySightedFlag.getFactory()),
			new SimpleImmutableEntry<FlagOpCode, IFlagFactory>(FlagOpCode.ASSIGN_PATROL, PatrolAssignmentFlag.getFactory()),
			new SimpleImmutableEntry<FlagOpCode, IFlagFactory>(FlagOpCode.TARGET_MISSING, TargetMissingFlag.getFactory())
	);
	
	public static Map<Class<? extends Flag.IFlag>, FlagOpCode> flagToOpCodeMap = new HashMap<>();
	public static Map<FlagOpCode, IFlagFactory> opCodeToFactoryMap = new HashMap<>();
	static {
		for (SimpleImmutableEntry<Class<? extends Flag.IFlag>, FlagOpCode> entry : TODOpairs) {
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
}
