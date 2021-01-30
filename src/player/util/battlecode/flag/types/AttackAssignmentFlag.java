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
import player.util.battlecode.flag.types.base.fields.DegreesField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.math.IntVec2D;

public class AttackAssignmentFlag extends BaseFlag {
	
	// used to decode fields via BaseFlag::decode
	private static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			MapLocField.getFactory()
	);
	
	// number of total bits in an encoded flag (precomputed/stored here for later)
	private static int NUM_BITS = fieldFactories.stream().mapToInt(factory -> factory.numBits()).sum();
	
	private MapLocField mapLocField;  // indicates the MapLocation to attack
	
	/**
	 * Commands units to attack the target at a MapLocation.
	 * Used by Enlightenment Centers after building (a) unit(s).
	 * 
	 * @param mapLoc a MapLocation on the map.
	 */
	public AttackAssignmentFlag(MapLocation mapLoc) {
		this.mapLocField = new MapLocField(mapLoc);
	}
	
	/**
	 * TODO(theimer): make sure this link works.
	 * See {@link AttackAssignmentFlag#AttackAssignmentFlag(MapLocation)}
	 * 
	 * @param flagFields the ordered fields of an AttackAssignmentFlag.
	 */
	private AttackAssignmentFlag(List<BaseFlag.IFlagField> flagFields) {
		this.mapLocField = (MapLocField)flagFields.get(0);
	}
	
	/**
	 * Decodes the bits of an encoded AttackAssignmentFlag into an AttackAssignmentFlag instance.
	 * 
	 * @param rawFlag must lie on [0, 2**MAX_NUM_BITS)
	 * @return an instance of AttackAssignmentFlag as described by the AttackAssignmentFlag
	 *     encoded within the argument bits.
	 */
	public static AttackAssignmentFlag decode(int rawFlag) {
		assert (rawFlag >= 0) && (rawFlag < (1 << Flag.MAX_NUM_BITS)) : "rawFlag: " + rawFlag;
		List<BaseFlag.IFlagField> fields = BaseFlag.decodeFields(rawFlag, fieldFactories);
		return new AttackAssignmentFlag(fields);
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
				return AttackAssignmentFlag.decode(bits);
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