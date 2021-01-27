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
import player.util.math.IntVec2D;

public class AttackAssignmentFlag extends BaseFlag {
	
	private static OpCode opCode = OpCode.ASSIGN_ATTACK;
	
	private static List<IFlagFieldFactory> fieldFactories = Arrays.asList(
			CoordField.getFactory()
	);
	
	private CoordField coord;
	
	public AttackAssignmentFlag(int x, int y) {
		this.coord = new CoordField(x, y);
	}
	
	private AttackAssignmentFlag(List<IFlagField> flagFields) {
		this.coord = (CoordField)flagFields.get(0);
	}
	
	public static AttackAssignmentFlag decode(int rawFlag) {
		List<IFlagField> fields = BaseFlag.decodeFields(rawFlag, fieldFactories);
		return new AttackAssignmentFlag(fields);
	}
	
	public IntVec2D getCoord() {
		return this.coord.value();
	}

	@Override
	public UtilFlag.OpCode getOpCode() {
		return opCode;
	}

	@Override
	protected List<IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.coord);
	}
}