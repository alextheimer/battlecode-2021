package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;
import java.util.stream.BaseStream;

import battlecode.common.RobotType;
import player.util.battlecode.flag.fields.CoordField;
import player.util.battlecode.flag.fields.RobotTypeField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.IFlagFieldFactory;
import player.util.battlecode.flag.util.UtilFlag.OpCode;
import player.util.math.IntVec2D;

public class EnemySightedFlag extends BaseFlag {
	
	public static OpCode opCode = OpCode.ENEMY_SIGHTED;
	public static List<IFlagFieldFactory> fieldFactories = Arrays.asList(
			RobotTypeField.getFactory(),
			CoordField.getFactory()
	);
	
	private static enum Field { RobotType, Coord }
	
	private RobotTypeField robotType;
	private CoordField coord;
	
	public EnemySightedFlag(RobotType robotType, int x, int y) {
		this.robotType = new RobotTypeField(robotType);
		this.coord = new CoordField(x, y);
	}
	
	private EnemySightedFlag(List<IFlagField> fieldList) {
		this.robotType = (RobotTypeField)fieldList.get(Field.RobotType.ordinal());
		this.coord = (CoordField)fieldList.get(Field.Coord.ordinal());
	}
	
	public IntVec2D getCoord() {
		return this.coord.value();
	}
	
	public RobotType getRobotType() {
		return this.robotType.value();
	}

	@Override
	protected List<IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.robotType, this.coord);
	}
	
	public static EnemySightedFlag decode(int rawFlag) {
		List<IFlagField> fields = BaseFlag.decodeFields(rawFlag, EnemySightedFlag.fieldFactories);
		return new EnemySightedFlag(fields);	
	}

	@Override
	public OpCode getOpCode() {
		return opCode;
	}
}