package player.util.battlecode.flag.types;

import player.util.battlecode.flag.fields.CoordField;
import player.util.battlecode.flag.fields.DegreesField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.OpCode;
import player.util.math.IntVec2D;

public class AttackAssignmentFlag implements UtilFlag.IFlag {
	private CoordField coord;
	
	public AttackAssignmentFlag(int x, int y) {
		this.coord = new CoordField(x, y);
	}
	
	private AttackAssignmentFlag(CoordField coord) {
		this.coord = coord;
	}
	
	public static AttackAssignmentFlag decode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		flagWalker.readBits(UtilFlag.numOpCodeBits);
		int coordBits = flagWalker.readBits(CoordField.NUM_BITS);
		CoordField coord = CoordField.fromBits(coordBits);
		return new AttackAssignmentFlag(coord);	
	}
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(UtilFlag.EMPTY_FLAG);
		flagWalker.writeBits(UtilFlag.numOpCodeBits, UtilFlag.OpCode.ASSIGN_ATTACK.ordinal());
		flagWalker.writeBits(DegreesField.NUM_BITS, this.coord.toBits());
		return flagWalker.getAllBits();
	}
	
	public IntVec2D getCoord() {
		return this.coord.value();
	}

	@Override
	public UtilFlag.OpCode getOpCode() {
		return UtilFlag.OpCode.ASSIGN_ATTACK;
	}
}