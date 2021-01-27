package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import player.util.battlecode.flag.fields.CoordField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.IFlagFieldFactory;
import player.util.battlecode.flag.util.UtilFlag.OpCode;
import player.util.math.IntVec2D;

public class TargetMissingFlag extends BaseFlag {
	
	public static OpCode opCode = OpCode.TARGET_MISSING;
	private static List<IFlagFieldFactory> fieldFactories = Arrays.asList(
			CoordField.getFactory()
	);
	
	private CoordField coord;
	
	public TargetMissingFlag(int x, int y) {
		this.coord = new CoordField(x, y);
	}
	public TargetMissingFlag(List<IFlagField> fields) {
		this.coord = (CoordField)fields.get(0);
	}
	public static TargetMissingFlag decode(int rawFlag) {
		List<IFlagField> fields = BaseFlag.decodeFields(rawFlag, fieldFactories);
		return new TargetMissingFlag(fields);
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