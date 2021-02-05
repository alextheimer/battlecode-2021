package player.util.battlecode.flag.util;

import java.util.HashMap;
import java.util.Map;

import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.EnemySightedFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;
import player.util.battlecode.flag.types.TargetMissingFlag;
import player.util.math.UtilMath;

/**
 * Contains flag-related utility types/constants/functions.
 */
public class UtilFlag {

	// stored before the encoded bits of an IFlag to indicate to Flag::decode how interpret the IFlag's bits
	public static enum FlagOpCode { EMPTY, ASSIGN_PATROL, TARGET_MISSING, ENEMY_SIGHTED, ASSIGN_ATTACK }

	static {
		// make sure the empty flag is represented correctly
		assert UtilFlag.getBitsFromOpCode(FlagOpCode.EMPTY) == Flag.EMPTY_FLAG :
			String.format("these values must be consistent: EMPTY:%d, EMPTY_FLAG:%d", UtilFlag.getBitsFromOpCode(FlagOpCode.EMPTY), Flag.EMPTY_FLAG);
	}

	// computed once; used to convert FlagOpCode bits to a FlagOpCode instance
	public static FlagOpCode FLAG_OP_CODE_VALUES[] = FlagOpCode.values();

	// number of bits used to encode each FlagOpCode
	public static int NUM_OP_CODE_BITS = UtilMath.log2Ceil(UtilFlag.FLAG_OP_CODE_VALUES.length);

	public static interface IFlagFactory {
		/**
		 * Decodes the bits of an encoded IFlag into an IFlag instance.
		 *
		 * @param bits must lie on [0, 2**numBits())
		 * @return an instance of IFlag as described by the IFlag encoded within the argument bits.
		 */
		public Flag.IFlag decode(int bits);

		/**
		 * Returns the number of bits used to encode the IFlag.
		 */
		public int numBits();
	}

	// Maps each IFlag classe to the FlagOpCode Flag::encode/::decode uses to identify it.
	public static Map<Class<? extends Flag.IFlag>, FlagOpCode> flagToOpCodeMap = new HashMap<>();

	// Maps each FlagOpCode to a factory object used to parse the encoded bits of an IFlag
	public static Map<FlagOpCode, IFlagFactory> opCodeToFactoryMap = new HashMap<>();

	static {
		// build flagToOpCodeMap -------------------------------------------------
		UtilFlag.flagToOpCodeMap.put(AttackAssignmentFlag.class, FlagOpCode.ASSIGN_ATTACK);
		UtilFlag.flagToOpCodeMap.put(EnemySightedFlag.class, FlagOpCode.ENEMY_SIGHTED);
		UtilFlag.flagToOpCodeMap.put(PatrolAssignmentFlag.class, FlagOpCode.ASSIGN_PATROL);
		UtilFlag.flagToOpCodeMap.put(TargetMissingFlag.class, FlagOpCode.TARGET_MISSING);

		// build opCodeToFactoryMap --------------------------------------------------------
		UtilFlag.opCodeToFactoryMap.put(FlagOpCode.ASSIGN_ATTACK, AttackAssignmentFlag.getFactory());
		UtilFlag.opCodeToFactoryMap.put(FlagOpCode.ENEMY_SIGHTED, EnemySightedFlag.getFactory());
		UtilFlag.opCodeToFactoryMap.put(FlagOpCode.ASSIGN_PATROL, PatrolAssignmentFlag.getFactory());
		UtilFlag.opCodeToFactoryMap.put(FlagOpCode.TARGET_MISSING, TargetMissingFlag.getFactory());
	}

	/**
	 * Returns true iff `bits` is positive and representable by `expectedNumBits` bits; else false.
	 * @param expectedNumBits must be > 0
	 */
	public static boolean validBits(final int expectedNumBits, final int bits) {
		assert expectedNumBits > 0 : "expectedNumBits: " + expectedNumBits;
		return (bits >= 0) && (UtilMath.log2Ceil(bits) <= expectedNumBits);
	}

	/**
	 * Returns the FlagOpCode encoded within a sequence of bits.
	 * @param bits the bits of an encoded FlagOpCode.
	 *     Must be non-negative and representable by no more than NUM_OP_CODE_BITS bits.
	 */
	public static FlagOpCode getOpCodeFromBits(final int bits) {
		assert (bits >= 0) && (UtilMath.log2Ceil(bits) <= UtilFlag.NUM_OP_CODE_BITS) : "bits: " + bits;
		return UtilFlag.FLAG_OP_CODE_VALUES[bits];
	}

	/**
	 * Returns a FlagOpCode as an encoded sequence of bits.
	 */
	public static int getBitsFromOpCode(final FlagOpCode opCode) {
		return opCode.ordinal();
	}
}
