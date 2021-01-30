package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import battlecode.common.MapLocation;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.battlecode.flag.types.base.fields.MapLocField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.math.IntVec2D;

public class TargetMissingFlag extends BaseFlag {
	
	// used to decode fields via BaseFlag::decode
	private static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			MapLocField.getFactory()
	);
	
	// number of total bits in an encoded flag (precomputed/stored here for later)
	private static int NUM_BITS = fieldFactories.stream().mapToInt(factory -> factory.numBits()).sum();
	
	private MapLocField mapLocField;  // the MapLocation from which an assigned target is missing
	
	/**
	 * Used by "attack-assigned" robots to indicate that their
	 * target does not exist at the assigned location.
	 * 
	 * @param mapLoc the location of the missing robot.
	 *     Must be valid (i.e. on the map).
	 */
	public TargetMissingFlag(MapLocation mapLoc) {
		this.mapLocField = new MapLocField(mapLoc);
	}
	
	/**
	 * TODO(theimer): make sure this link works.
	 * See {@link TargetMissingFlag#TargetMissingFlag(MapLocation)}
	 * 
	 * @param fieldList the ordered fields of a TargetMissingFlag.
	 */
	public TargetMissingFlag(List<BaseFlag.IFlagField> fields) {
		this.mapLocField = (MapLocField)fields.get(0);
	}
	
	/**
	 * Decodes the bits of an encoded TargetMissingFlag into a TargetMissingFlag instance.
	 * 
	 * @param rawFlag must lie on [0, 2**MAX_NUM_BITS)
	 * @return an instance of TargetMissingFlag as described by the TargetMissingFlag
	 *     encoded within the argument bits.
	 */
	public static TargetMissingFlag decode(int rawFlag) {
		assert (rawFlag >= 0) && (rawFlag < (1 << Flag.MAX_NUM_BITS)) : "rawFlag: " + rawFlag;
		List<BaseFlag.IFlagField> fields = BaseFlag.decodeFields(rawFlag, fieldFactories);
		return new TargetMissingFlag(fields);
	}

	/**
	 * Returns the MapLocation indicated by the flag.
	 * 
	 * @param referenceMapLoc *any* valid (i.e. on the map) MapLocation.
	 */
	public MapLocation getMapLoc(MapLocation referenceMapLoc) {
		return this.mapLocField.getMapLocation(referenceMapLoc);
	}
	
	@Override
	protected List<BaseFlag.IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.mapLocField);
	}
	public static IFlagFactory getFactory() {
		return new IFlagFactory() {

			@Override
			public Flag.IFlag decode(int bits) {
				return TargetMissingFlag.decode(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};	
	}
	
	@Override
	public int numBits() {
		return NUM_BITS;
	}
}