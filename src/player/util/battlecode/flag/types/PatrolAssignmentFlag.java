package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import player.util.battlecode.flag.fields.CoordField;
import player.util.battlecode.flag.fields.DegreesField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.IFlagFieldFactory;
import player.util.battlecode.flag.util.UtilFlag.OpCode;

public class PatrolAssignmentFlag extends BaseFlag {
	
	private static OpCode opCode = OpCode.ASSIGN_PATROL;
	private static List<IFlagFieldFactory> fieldFactories = Arrays.asList(
			DegreesField.getFactory()
	);
	
	private DegreesField outboundDegrees;
	
	public PatrolAssignmentFlag(int outboundDegrees) {
		this.outboundDegrees = new DegreesField(outboundDegrees);
	}
	
	private PatrolAssignmentFlag(List<IFlagField> fieldList) {
		this.outboundDegrees = (DegreesField)fieldList.get(0);
	}
	
	public static PatrolAssignmentFlag decode(int rawFlag) {
		List<IFlagField> fields = BaseFlag.decodeFields(rawFlag, fieldFactories);
		return new PatrolAssignmentFlag(fields);
	}
	
	public int getOutboundDegrees() {
		return this.outboundDegrees.value();
	}

	@Override
	public UtilFlag.OpCode getOpCode() {
		return opCode;
	}

	@Override
	protected List<IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.outboundDegrees);
	}
}