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
import player.util.math.UtilMath;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;

public class PatrolAssignmentFlag extends BaseFlag {
	
	// used to decode fields via BaseFlag::decode
	private static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			DegreesField.getFactory()
	);
	
	// number of total bits in an encoded flag (precomputed/stored here for later)
	private static int NUM_BITS = fieldFactories.stream().mapToInt(factory -> factory.numBits()).sum();
	
	// Indicates the direction to travel from the receiving robot's initial location.
	// "Outbound" because a patrolling robot will eventually turn around to travel in the opposite direction.
	private DegreesField outboundDegrees;
	
	/**
	 * Commands units to patrol along a line in a certain direction.
	 * TODO(theimer): additional info...
	 * Used by Enlightenment Centers after building (a) unit(s).
	 * 
	 * @param outboundDegrees the initial direction to travel.
	 *     Must lie on [0, 360);
	 */
	public PatrolAssignmentFlag(int outboundDegrees) {
		assert (outboundDegrees >= 0) && (outboundDegrees < UtilMath.CIRCLE_DEGREES) :
			"outboundDegrees: " + outboundDegrees;
		this.outboundDegrees = new DegreesField(outboundDegrees);
	}
	
	/**
	 * TODO(theimer): make sure this link works.
	 * See {@link PatrolAssignmentFlag#PatrolAssignmentFlag(int)}
	 * 
	 * @param fieldList the ordered fields of a PatrolAssignmentFlag.
	 */
	private PatrolAssignmentFlag(List<BaseFlag.IFlagField> fieldList) {
		this.outboundDegrees = (DegreesField)fieldList.get(0);
	}
	
	/**
	 * Decodes the bits of an encoded PatrolAssignmentFlag into a PatrolAssignmentFlag instance.
	 * 
	 * @param rawFlag must lie on [0, 2**MAX_NUM_BITS)
	 * @return an instance of PatrolAssignmentFlag as described by the PatrolAssignmentFlag
	 *     encoded within the argument bits.
	 */
	public static PatrolAssignmentFlag decode(int rawFlag) {
		assert (rawFlag >= 0) && (rawFlag < (1 << Flag.MAX_NUM_BITS)) : "rawFlag: " + rawFlag;
		List<BaseFlag.IFlagField> fields = BaseFlag.decodeFields(rawFlag, fieldFactories);
		return new PatrolAssignmentFlag(fields);
	}
	
	public int getOutboundDegrees() {
		return this.outboundDegrees.getDegrees();
	}

	@Override
	protected List<BaseFlag.IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.outboundDegrees);
	}
	
	public static IFlagFactory getFactory() {
		return new IFlagFactory() {

			@Override
			public Flag.IFlag decode(int bits) {
				return PatrolAssignmentFlag.decode(bits);
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