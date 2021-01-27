package player.util.battlecode.flag.types;

import battlecode.common.RobotType;
import player.util.battlecode.flag.fields.CoordField;
import player.util.battlecode.flag.fields.RobotTypeField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.OpCode;
import player.util.math.IntVec2D;

public class EnemySightedFlag implements UtilFlag.IFlag {
	private RobotTypeField robotType;
	private CoordField coord;
	
	public EnemySightedFlag(RobotType robotType, int x, int y) {
		this.robotType = new RobotTypeField(robotType);
		this.coord = new CoordField(x, y);
	}
	
	private EnemySightedFlag(RobotTypeField robotType, CoordField coordField) {
		this.robotType = robotType;
		this.coord = coordField;
	}
	
	public static EnemySightedFlag decode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		flagWalker.readBits(UtilFlag.numOpCodeBits);
		int robotTypeBits = flagWalker.readBits(RobotTypeField.NUM_BITS);
		int coordBits = flagWalker.readBits(CoordField.NUM_BITS);
		RobotTypeField robotType = RobotTypeField.fromBits(robotTypeBits);
		CoordField coord = CoordField.fromBits(coordBits);
		return new EnemySightedFlag(robotType, coord);	
	}
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(UtilFlag.EMPTY_FLAG);
		flagWalker.writeBits(UtilFlag.numOpCodeBits, UtilFlag.OpCode.ENEMY_SIGHTED.ordinal());
		flagWalker.writeBits(RobotTypeField.NUM_BITS, this.robotType.toBits());
		flagWalker.writeBits(CoordField.NUM_BITS, this.coord.toBits());
		return flagWalker.getAllBits();
	}
	
	public IntVec2D getCoord() {
		return this.coord.value();
	}
	
	public RobotType getRobotType() {
		return this.robotType.value();
	}

	@Override
	public UtilFlag.OpCode getOpCode() {
		return UtilFlag.OpCode.ENEMY_SIGHTED;
	}
}