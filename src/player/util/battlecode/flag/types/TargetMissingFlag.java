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
	// note: the Battlecode backend doesn't like "mapToInt" or "sum".
	private static int NUM_BITS =
			TargetMissingFlag.fieldFactories.stream().map(factory -> factory.numBits()).reduce(0, (a, b) -> a + b);

	private final MapLocField mapLocField;  // the MapLocation from which an assigned target is missing

	/**
	 * Used by "attack-assigned" robots to indicate that their
	 * target does not exist at the assigned location.
	 *
	 * @param mapLoc the location of the missing robot.
	 *     Must be valid (i.e. on the map).
	 */
	public TargetMissingFlag(final MapLocation mapLoc) {
		this.mapLocField = new MapLocField(mapLoc);
	}

	/**
	 * See {@link TargetMissingFlag#TargetMissingFlag(MapLocation)}
	 *
	 * @param fields the ordered fields of a TargetMissingFlag.
	 */
	public TargetMissingFlag(final List<BaseFlag.IFlagField> fields) {
		this.mapLocField = (MapLocField)fields.get(0);
	}

	/**
	 * Returns the MapLocation indicated by the flag.
	 *
	 * @param referenceMapLoc *any* valid (i.e. on the map) MapLocation.
	 */
	public MapLocation getMapLoc(final MapLocation referenceMapLoc) {
		return this.mapLocField.getMapLocation(referenceMapLoc);
	}

	@Override
	protected List<BaseFlag.IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.mapLocField);
	}
	public static IFlagFactory getFactory() {
		return new IFlagFactory() {

			@Override
			public Flag.IFlag decode(final int bits) {
				assert UtilFlag.validBits(TargetMissingFlag.NUM_BITS, bits) : "bits: " + bits;
				final List<BaseFlag.IFlagField> fields =
						BaseFlag.decodeFields(bits, TargetMissingFlag.fieldFactories);
				return new TargetMissingFlag(fields);
			}

			@Override
			public int numBits() {
				return TargetMissingFlag.NUM_BITS;
			}

		};
	}

	@Override
	public int numBits() {
		return TargetMissingFlag.NUM_BITS;
	}
}