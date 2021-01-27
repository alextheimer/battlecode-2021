package player.util.battlecode.flag.types;

import player.util.battlecode.flag.fields.DegreesField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.OpCode;

public class PatrolAssignmentFlag implements UtilFlag.IFlag {
	private DegreesField outboundDegrees;
	
	public PatrolAssignmentFlag(int outboundDegrees) {
		this.outboundDegrees = new DegreesField(outboundDegrees);
	}
	
	private PatrolAssignmentFlag(DegreesField outboundDegrees) {
		this.outboundDegrees = outboundDegrees;
	}
	
	public static PatrolAssignmentFlag decode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		flagWalker.readBits(UtilFlag.numOpCodeBits);
		int outboundDegreesBits = flagWalker.readBits(DegreesField.NUM_BITS);
		DegreesField outboundDegrees = DegreesField.fromBits(outboundDegreesBits);
		return new PatrolAssignmentFlag(outboundDegrees);	
	}
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(UtilFlag.EMPTY_FLAG);
		flagWalker.writeBits(UtilFlag.numOpCodeBits, UtilFlag.OpCode.ASSIGN_PATROL.ordinal());
		flagWalker.writeBits(DegreesField.NUM_BITS, this.outboundDegrees.toBits());
		return flagWalker.getAllBits();
	}
	
	public int getOutboundDegrees() {
		return this.outboundDegrees.value();
	}

	@Override
	public UtilFlag.OpCode getOpCode() {
		return UtilFlag.OpCode.ASSIGN_PATROL;
	}
}