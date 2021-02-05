package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import battlecode.common.MapLocation;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.fields.MapLocField;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;

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
				assert UtilFlag.validBits(NUM_BITS, bits) : "bits: " + bits;
				List<BaseFlag.IFlagField> fields = BaseFlag.decodeFields(bits, fieldFactories);
				return new TargetMissingFlag(fields);
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