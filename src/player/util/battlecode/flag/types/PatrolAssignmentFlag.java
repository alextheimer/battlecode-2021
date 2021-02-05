package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.fields.DegreesField;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;
import player.util.math.UtilMath;

public class PatrolAssignmentFlag extends BaseFlag {

	// used to decode fields via BaseFlag::decode
	private static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			DegreesField.getFactory()
	);

	// number of total bits in an encoded flag (precomputed/stored here for later)
	// note: the Battlecode backend doesn't like "mapToInt" or "sum".
	private static int NUM_BITS =
			PatrolAssignmentFlag.fieldFactories.stream().map(factory -> factory.numBits()).reduce(0, (a, b) -> a + b);

	// Indicates the direction to travel from the receiving robot's initial location.
	// "Outbound" because a patrolling robot will eventually turn around to travel in the opposite direction.
	private final DegreesField outboundDegrees;

	/**
	 * Commands units to patrol along a line in a certain direction.
	 * TODO(theimer): additional info...
	 * Used by Enlightenment Centers after building (a) unit(s).
	 *
	 * @param outboundDegrees the initial direction to travel.
	 *     Must lie on [0, 360);
	 */
	public PatrolAssignmentFlag(final int outboundDegrees) {
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
	private PatrolAssignmentFlag(final List<BaseFlag.IFlagField> fieldList) {
		this.outboundDegrees = (DegreesField)fieldList.get(0);
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
			public Flag.IFlag decode(final int bits) {
				assert UtilFlag.validBits(PatrolAssignmentFlag.NUM_BITS, bits) : "bits: " + bits;
				final List<BaseFlag.IFlagField> fields =
						BaseFlag.decodeFields(bits, PatrolAssignmentFlag.fieldFactories);
				return new PatrolAssignmentFlag(fields);
			}

			@Override
			public int numBits() {
				return PatrolAssignmentFlag.NUM_BITS;
			}
		};
	}

	@Override
	public int numBits() {
		return PatrolAssignmentFlag.NUM_BITS;
	}
}