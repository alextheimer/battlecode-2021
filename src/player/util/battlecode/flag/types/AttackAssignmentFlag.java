package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import battlecode.common.MapLocation;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.fields.MapLocField;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;

public class AttackAssignmentFlag extends BaseFlag {

	// used to decode fields via BaseFlag::decode
	private static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			MapLocField.getFactory()
	);

	// number of total bits in an encoded flag (precomputed/stored here for later)
	// note: the Battlecode backend doesn't like "mapToInt" or "sum".
	private static int NUM_BITS =
			AttackAssignmentFlag.fieldFactories.stream().map(factory -> factory.numBits()).reduce(0, (a, b) -> a + b);

	private final MapLocField mapLocField;  // indicates the MapLocation to attack

	/**
	 * Commands units to attack the target at a MapLocation.
	 * Used by Enlightenment Centers after building (a) unit(s).
	 *
	 * @param mapLoc a MapLocation on the map.
	 */
	public AttackAssignmentFlag(final MapLocation mapLoc) {
		this.mapLocField = new MapLocField(mapLoc);
	}

	/**
	 * See {@link AttackAssignmentFlag#AttackAssignmentFlag(MapLocation)}
	 *
	 * @param flagFields the ordered fields of an AttackAssignmentFlag.
	 */
	private AttackAssignmentFlag(final List<BaseFlag.IFlagField> flagFields) {
		this.mapLocField = (MapLocField)flagFields.get(0);
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
				assert UtilFlag.validBits(AttackAssignmentFlag.NUM_BITS, bits) : "" + bits;
				final List<BaseFlag.IFlagField> fields =
						BaseFlag.decodeFields(bits, AttackAssignmentFlag.fieldFactories);
				return new AttackAssignmentFlag(fields);
			}

			@Override
			public int numBits() {
				return AttackAssignmentFlag.NUM_BITS;
			}

		};
	}

	@Override
	public int numBits() {
		return AttackAssignmentFlag.NUM_BITS;
	}
}