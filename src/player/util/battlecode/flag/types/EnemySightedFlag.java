package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.fields.MapLocField;
import player.util.battlecode.flag.types.base.fields.RobotTypeField;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;

public class EnemySightedFlag extends BaseFlag {

	// used to decode fields via BaseFlag::decode
	public static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			RobotTypeField.getFactory(),
			MapLocField.getFactory()
	);

	// number of total bits in an encoded flag (precomputed/stored here for later)
	private static int NUM_BITS =
			EnemySightedFlag.fieldFactories.stream().mapToInt(factory -> factory.numBits()).sum();

	// ordinal() acts as an index into ordered field lists.
	private enum Field { ROBOT_TYPE, MAP_LOC }

	private final RobotTypeField robotTypeField;  // the type of robot sighted
	private final MapLocField mapLocField;  // the location at which it was sighted

	/**
	 * Indicates to other units that an enemy (TODO(theimer): non-team?) unit was
	 * sighted. Details the robot's type and location.
	 *
	 * @param robotType the type of the sighted robot.
	 * @param mapLoc the location of the sighted robot.
	 *     Must be valid (i.e. on the map).
	 */
	public EnemySightedFlag(final RobotType robotType, final MapLocation mapLoc) {
		this.robotTypeField = new RobotTypeField(robotType);
		this.mapLocField = new MapLocField(mapLoc);
	}

	/**
	 * TODO(theimer): make sure this link works.
	 * See {@link EnemySightedFlag#EnemySightedFlag(MapLocation)}
	 *
	 * @param fieldList the ordered fields of an EnemySightedFlag.
	 */
	private EnemySightedFlag(final List<BaseFlag.IFlagField> fieldList) {
		this.robotTypeField = (RobotTypeField)fieldList.get(Field.ROBOT_TYPE.ordinal());
		this.mapLocField = (MapLocField)fieldList.get(Field.MAP_LOC.ordinal());
	}

	/**
	 * Returns the MapLocation indicated by the flag.
	 *
	 * @param referenceMapLoc *any* valid (i.e. on the map) MapLocation.
	 */
	public MapLocation getMapLoc(final MapLocation referenceMapLoc) {
		return this.mapLocField.getMapLocation(referenceMapLoc);
	}

	public RobotType getRobotType() {
		return this.robotTypeField.getRobotType();
	}

	@Override
	protected List<BaseFlag.IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.robotTypeField, this.mapLocField);
	}

	public static IFlagFactory getFactory() {
		return new IFlagFactory() {

			@Override
			public Flag.IFlag decode(final int bits) {
				assert UtilFlag.validBits(EnemySightedFlag.NUM_BITS, bits) : "" + bits;
				final List<BaseFlag.IFlagField> fields =
						BaseFlag.decodeFields(bits, EnemySightedFlag.fieldFactories);
				return new EnemySightedFlag(fields);
			}

			@Override
			public int numBits() {
				return EnemySightedFlag.NUM_BITS;
			}

		};
	}

	@Override
	public int numBits() {
		return EnemySightedFlag.NUM_BITS;
	}
}