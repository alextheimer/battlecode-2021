package player.util.battlecode.flag;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.RobotType;
import player.util.math.IntVec2D;
import player.util.math.UtilMath;
import player.util.math.UtilMath.*;
import player.handlers.common.HandlerCommon.*;
import player.util.battlecode.flag.FlagFields.*;
import player.util.general.UtilGeneral.*;

public class Flag {
	public static enum OpCode {EMPTY, ASSIGN_PATROL, TARGET_MISSING, FOLLOWER_CLAIM, ENEMY_SIGHTED, ASSIGN_ATTACK, BASE_SIGHTED};
	public static int EMPTY_FLAG = 0;
	public static int NUM_BITS = 24;
	
	public static interface IFlag {
		public OpCode getOpCode();
		public int encode();
	}
	
	private static OpCode opCodeValues[] = OpCode.values();
	private static int numOpCodeBits = UtilMath.log2Ceil(opCodeValues.length);
	
	public static OpCode getOpCode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		int opCodeID = flagWalker.readBits(numOpCodeBits);
		return opCodeValues[opCodeID];
	}
	
	public static class FollowerClaimFlag implements IFlag {
		public static FollowerClaimFlag decode(int rawFlag) {return new FollowerClaimFlag();}
		public int encode() {return OpCode.FOLLOWER_CLAIM.ordinal();}
		@Override
		public OpCode getOpCode() {
			return OpCode.FOLLOWER_CLAIM;
		}
	}
	
	public static class TargetMissingFlag implements IFlag {
		private CoordField coord;
		public TargetMissingFlag(int x, int y) {
			this.coord = new CoordField(x, y);
		}
		public TargetMissingFlag(CoordField coord) {
			this.coord = coord;
		}
		public static TargetMissingFlag decode(int rawFlag) {
			FlagWalker flagWalker = new FlagWalker(rawFlag);
			flagWalker.readBits(numOpCodeBits);
			int coordBits = flagWalker.readBits(CoordField.NUM_BITS);
			CoordField coord = CoordField.fromBits(coordBits);
			return new TargetMissingFlag(coord);
		}
		public int encode() {
			FlagWalker flagWalker = new FlagWalker(Flag.EMPTY_FLAG);
			flagWalker.writeBits(numOpCodeBits, OpCode.TARGET_MISSING.ordinal());
			flagWalker.writeBits(CoordField.NUM_BITS, coord.toBits());
			return flagWalker.getAllBits();
		}
		public IntVec2D getCoord() {
			return this.coord.value();
		}
		@Override
		public OpCode getOpCode() {
			return OpCode.TARGET_MISSING;
		}
	}
	
	public static class PatrolAssignmentFlag implements IFlag {
		private DegreesField outboundDegrees;
		
		public PatrolAssignmentFlag(int outboundDegrees) {
			this.outboundDegrees = new DegreesField(outboundDegrees);
		}
		
		private PatrolAssignmentFlag(DegreesField outboundDegrees) {
			this.outboundDegrees = outboundDegrees;
		}
		
		public static PatrolAssignmentFlag decode(int rawFlag) {
			FlagWalker flagWalker = new FlagWalker(rawFlag);
			flagWalker.readBits(numOpCodeBits);
			int outboundDegreesBits = flagWalker.readBits(DegreesField.NUM_BITS);
			DegreesField outboundDegrees = DegreesField.fromBits(outboundDegreesBits);
			return new PatrolAssignmentFlag(outboundDegrees);	
		}
		public int encode() {
			FlagWalker flagWalker = new FlagWalker(EMPTY_FLAG);
			flagWalker.writeBits(numOpCodeBits, OpCode.ASSIGN_PATROL.ordinal());
			flagWalker.writeBits(DegreesField.NUM_BITS, this.outboundDegrees.toBits());
			return flagWalker.getAllBits();
		}
		
		public int getOutboundDegrees() {
			return this.outboundDegrees.value();
		}

		@Override
		public OpCode getOpCode() {
			return OpCode.ASSIGN_PATROL;
		}
	}
	
	public static class AttackAssignmentFlag implements IFlag {
		private CoordField coord;
		
		public AttackAssignmentFlag(int x, int y) {
			this.coord = new CoordField(x, y);
		}
		
		private AttackAssignmentFlag(CoordField coord) {
			this.coord = coord;
		}
		
		public static AttackAssignmentFlag decode(int rawFlag) {
			FlagWalker flagWalker = new FlagWalker(rawFlag);
			flagWalker.readBits(numOpCodeBits);
			int coordBits = flagWalker.readBits(CoordField.NUM_BITS);
			CoordField coord = CoordField.fromBits(coordBits);
			return new AttackAssignmentFlag(coord);	
		}
		public int encode() {
			FlagWalker flagWalker = new FlagWalker(EMPTY_FLAG);
			flagWalker.writeBits(numOpCodeBits, OpCode.ASSIGN_ATTACK.ordinal());
			flagWalker.writeBits(DegreesField.NUM_BITS, this.coord.toBits());
			return flagWalker.getAllBits();
		}
		
		public IntVec2D getCoord() {
			return this.coord.value();
		}

		@Override
		public OpCode getOpCode() {
			return OpCode.ASSIGN_ATTACK;
		}
	}
	
	public static class EnemySightedFlag implements IFlag {
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
			flagWalker.readBits(numOpCodeBits);
			int robotTypeBits = flagWalker.readBits(RobotTypeField.NUM_BITS);
			int coordBits = flagWalker.readBits(CoordField.NUM_BITS);
			RobotTypeField robotType = RobotTypeField.fromBits(robotTypeBits);
			CoordField coord = CoordField.fromBits(coordBits);
			return new EnemySightedFlag(robotType, coord);	
		}
		public int encode() {
			FlagWalker flagWalker = new FlagWalker(EMPTY_FLAG);
			flagWalker.writeBits(numOpCodeBits, OpCode.ENEMY_SIGHTED.ordinal());
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
		public OpCode getOpCode() {
			return OpCode.ENEMY_SIGHTED;
		}
	}
}
