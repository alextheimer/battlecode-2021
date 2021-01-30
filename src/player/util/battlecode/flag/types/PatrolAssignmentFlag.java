package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

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

public class PatrolAssignmentFlag extends BaseFlag {
	
	private static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			DegreesField.getFactory()
	);
	
	private static int NUM_BITS = fieldFactories.stream().mapToInt(factory -> factory.numBits()).sum();
	
	private DegreesField outboundDegrees;
	
	public PatrolAssignmentFlag(int outboundDegrees) {
		this.outboundDegrees = new DegreesField(outboundDegrees);
	}
	
	private PatrolAssignmentFlag(List<BaseFlag.IFlagField> fieldList) {
		this.outboundDegrees = (DegreesField)fieldList.get(0);
	}
	
	public static PatrolAssignmentFlag decode(int rawFlag) {
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