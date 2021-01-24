package player.util;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.RobotType;
import player.handlers.HandlerCommon.*;
import player.util.Util.*;
import player.util.FlagFields.*;
import player.util.UtilMath.*;

/**
 * Sequentially builds / reads (i.e. "walk") a flag.
 * Flags are built/read from the least-to-most significant bits.
 */
class FlagWalker {
	public static int MAX_NUM_BITS = 24; // The spec limits flags to 24 bits.
	
	private int flag;  // the flag we're reading/modifying
	private int nextBitIndex;  // the next unread & unwritten bit index
	
	public FlagWalker(int flag) {
		this.flag = flag;
		this.nextBitIndex = 0;  // indices increase from right to left.
	}
	
	/**
	 * Writes bits to the flag such that the least significant bit of 'bits' is positined at the
	 * next unread & unwritten bit index.
	 */
	public void writeBits(int numBits, int bits) {
		// TODO(theimer): assert stuff
		int mask = ((1 << numBits) - 1) << this.nextBitIndex;
		// TODO(theimer): assert mask stuff
		this.flag &= ~(mask << this.nextBitIndex);
		this.flag |= (bits << this.nextBitIndex);
		this.nextBitIndex += numBits;
	}
	
	/**
	 * Reads bits from the flag such that the least significant bit of the 'numBits' bits to be read
	 * is positioned at the next unread & unwritten bit index.
	 */
	public int readBits(int numBits) {
		int mask = ((1 << numBits) - 1) << this.nextBitIndex;
		// TODO(theimer): assert mask stuff
		int result = (this.flag & mask) >> this.nextBitIndex;
		this.nextBitIndex += numBits;
		return result;
	}
	
	/**
	 * Returns the flag.
	 * Does not affect the values returned by readBits or writeBits.
	 */
	public int getAllBits() {
		return this.flag;
	}
}

/**
 * 
 */
class FlagFields {
	public static class DiffVecField {
		public static final int NUM_BITS = 8;
		
		private static final int numBitsPerDim = 4;
		private static final int offset = 6;
		
		private IntVec2D vec;
		
		public DiffVecField(int x, int y) {
			// TODO(theimer): assertions
			this.vec = new IntVec2D(x, y);
		}
		
		public int toBits() {
			FlagWalker flagWalker = new FlagWalker(0);
			flagWalker.writeBits(numBitsPerDim, this.vec.x + offset);
			flagWalker.writeBits(numBitsPerDim, this.vec.y + offset);
			return flagWalker.getAllBits();
		}
		
		public static DiffVecField fromBits(int bits) {
			FlagWalker flagWalker = new FlagWalker(bits);
			int x = flagWalker.readBits(numBitsPerDim) - offset;
			int y = flagWalker.readBits(numBitsPerDim) - offset;
			return new DiffVecField(x, y);
		}
		
		public IntVec2D value() {
			return this.vec;
		}
	}
	
	public static class DirectionField {
		public static final int NUM_BITS = 3;
		
		private static Map<Direction, Integer> dirToIndexMap;
		private static Map<Integer, Direction> indexToDirMap;
		
		static {
			List<SimpleImmutableEntry<Direction, Integer>> dirIndexPairs = Arrays.asList(
					new SimpleImmutableEntry<>(Direction.NORTH, 0),
					new SimpleImmutableEntry<>(Direction.NORTH, 1),
					new SimpleImmutableEntry<>(Direction.NORTH, 2),
					new SimpleImmutableEntry<>(Direction.NORTH, 3),
					new SimpleImmutableEntry<>(Direction.NORTH, 4),
					new SimpleImmutableEntry<>(Direction.NORTH, 5),
					new SimpleImmutableEntry<>(Direction.NORTH, 6),
					new SimpleImmutableEntry<>(Direction.NORTH, 7)
			);
			for (SimpleImmutableEntry<Direction, Integer> entry : dirIndexPairs) {
				dirToIndexMap.put(entry.getKey(), entry.getValue());
				indexToDirMap.put(entry.getValue(), entry.getKey());
			}
		}
		
		private Direction direction;
		
		public DirectionField(Direction direction) {
			// TODO(theimer): assertions
			this.direction = direction;
		}
		
		public int toBits() {
			FlagWalker flagWalker = new FlagWalker(0);
			flagWalker.writeBits(NUM_BITS, DirectionField.dirToIndexMap.get(this.direction));
			return flagWalker.getAllBits();
		}
		
		public static DirectionField fromBits(int bits) {
			FlagWalker flagWalker = new FlagWalker(bits);
			int index = flagWalker.readBits(NUM_BITS);
			return new DirectionField(DirectionField.indexToDirMap.get(index));
		}
		
		public Direction value() {
			return this.direction;
		}
	}
	
	public static class DegreesField {
		public static final int NUM_BITS = 9;
		
		private int degrees;
		
		public DegreesField(int degrees) {
			// TODO(theimer): assertions
			this.degrees = degrees;
		}
		
		public int toBits() {
			return this.degrees;
		}
		
		public static DegreesField fromBits(int bits) {
			return new DegreesField(bits);
		}
		
		public int value() {
			return this.degrees;
		}
	}
	
	public static class CoordField {
		public static final int NUM_BITS = 14;
		private static final int NUM_BITS_PER = 7;
		
		private int x;
		private int y;
		
		public CoordField(int x, int y) {
			// TODO(theimer): assertions
			this.x = x;
			this.y = y;
		}
		
		public int toBits() {
			FlagWalker flagWalker = new FlagWalker(0);
			flagWalker.writeBits(NUM_BITS_PER, this.x);
			flagWalker.writeBits(NUM_BITS_PER, this.y);
			return flagWalker.getAllBits();
		}
		
		public static CoordField fromBits(int bits) {
			FlagWalker flagWalker = new FlagWalker(bits);
			int x = flagWalker.readBits(NUM_BITS_PER);
			int y = flagWalker.readBits(NUM_BITS_PER);
			return new CoordField(x, y);
		}
		
		public IntVec2D value() {
			return new IntVec2D(this.x, this.y);
		}
	}
	
	public static class SquadTypeField {
		public static final int NUM_BITS = UtilMath.numBits(AssignmentType.values().length);
		
		private AssignmentType squadType;
		
		public SquadTypeField(AssignmentType squadType) {
			// TODO(theimer): assertions
			this.squadType = squadType;
		}
		
		public int toBits() {
			return this.squadType.ordinal();
		}
		
		public static SquadTypeField fromBits(int bits) {
			// TODO(theimer): !!!!!!!!!!
			return new SquadTypeField(AssignmentType.values()[bits]);
		}
		
		public AssignmentType value() {
			return this.squadType;
		}
	}
	
	public static class RobotTypeField {
		public static final int NUM_BITS = UtilMath.numBits(RobotType.values().length);
		
		private RobotType robotType;
		
		public RobotTypeField(RobotType robotType) {
			// TODO(theimer): assertions
			this.robotType = robotType;
		}
		
		public int toBits() {
			return this.robotType.ordinal();
		}
		
		public static RobotTypeField fromBits(int bits) {
			// TODO(theimer): !!!!!!!!!!
			return new RobotTypeField(RobotType.values()[bits]);
		}
		
		public RobotType value() {
			return this.robotType;
		}
	}
	
	public static class IdField {
		public static final int NUM_BITS = 15;
		
		private int id;
		
		public IdField(int id) {
			// TODO(theimer): assertions
			this.id = id;
		}
		
		public int toBits() {
			return this.id;
		}
		
		public static IdField fromBits(int bits) {
			// TODO(theimer): !!!!!!!!!!
			return new IdField(bits);
		}
		
		public int value() {
			return this.id;
		}
	}
	
}

public class Flag {
	public static enum OpCode {EMPTY, LEADER_CLAIM, FOLLOWER_CLAIM, ASSIGNMENT, ENEMY_SIGHTED, ATTACK_TARGET, BASE_SIGHTED};
	public static int EMPTY_FLAG = 0;
	public static int NUM_BITS = 24;
	
	private static OpCode opCodeValues[] = OpCode.values();
	private static int numOpCodeBits = UtilMath.numBits(opCodeValues.length);
	
	public static OpCode getOpCode(int rawFlag) {
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		int opCodeID = flagWalker.readBits(numOpCodeBits);
		return opCodeValues[opCodeID];
	}
	
	public static class FollowerClaimFlag {
		public static FollowerClaimFlag decode(int rawFlag) {return new FollowerClaimFlag();}
		public int encode() {return OpCode.FOLLOWER_CLAIM.ordinal();}
	}
	
	public static class LeaderClaimFlag {
		public static LeaderClaimFlag decode(int rawFlag) {return new LeaderClaimFlag();}
		public int encode() {return OpCode.LEADER_CLAIM.ordinal();}
	}
	
	public static class AssignmentFlag {
		private SquadTypeField squadType;
		private DegreesField outboundDegrees;
		
		public AssignmentFlag(AssignmentType squadType, int outboundDegrees) {
			this.squadType = new SquadTypeField(squadType);
			this.outboundDegrees = new DegreesField(outboundDegrees);
		}
		
		private AssignmentFlag(SquadTypeField squadType, DegreesField outboundDegrees) {
			this.squadType = squadType;
			this.outboundDegrees = outboundDegrees;
		}
		
		public static AssignmentFlag decode(int rawFlag) {
			FlagWalker flagWalker = new FlagWalker(rawFlag);
			flagWalker.readBits(numOpCodeBits);
			int squadTypeBits = flagWalker.readBits(SquadTypeField.NUM_BITS);
			int outboundDegreesBits = flagWalker.readBits(DegreesField.NUM_BITS);
			SquadTypeField squadType = SquadTypeField.fromBits(squadTypeBits);
			DegreesField outboundDegrees = DegreesField.fromBits(outboundDegreesBits);
			return new AssignmentFlag(squadType, outboundDegrees);	
		}
		public int encode() {
			FlagWalker flagWalker = new FlagWalker(EMPTY_FLAG);
			flagWalker.writeBits(numOpCodeBits, OpCode.ASSIGNMENT.ordinal());
			flagWalker.writeBits(SquadTypeField.NUM_BITS, this.squadType.toBits());
			flagWalker.writeBits(DegreesField.NUM_BITS, this.outboundDegrees.toBits());
			return flagWalker.getAllBits();
		}
		
		public int getOutboundDegrees() {
			return this.outboundDegrees.value();
		}
		
		public AssignmentType getAssignmentType() {
			return this.squadType.value();
		}
	}
	
	public static class EnemySightedFlag {
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
	}

	public static class AttackTargetFlag {
		private IdField idField;
		private DirectionField dirField;
		public AttackTargetFlag(int id, Direction dir) {
			this.idField = new IdField(id);
			this.dirField = new DirectionField(dir);
		}
		private AttackTargetFlag(IdField idField, DirectionField dirField) {
			this.idField = idField;
			this.dirField = dirField;
		}
		public static AttackTargetFlag decode(int rawFlag) {
			FlagWalker flagWalker = new FlagWalker(rawFlag);
			flagWalker.readBits(numOpCodeBits);
			int idBits = flagWalker.readBits(IdField.NUM_BITS);
			int dirBits = flagWalker.readBits(DirectionField.NUM_BITS);
			return new AttackTargetFlag(IdField.fromBits(idBits), DirectionField.fromBits(dirBits));
		}
		public int encode() {
			FlagWalker flagWalker = new FlagWalker(EMPTY_FLAG);
			flagWalker.writeBits(numOpCodeBits, OpCode.ATTACK_TARGET.ordinal());
			flagWalker.writeBits(IdField.NUM_BITS, this.idField.toBits());
			flagWalker.writeBits(DirectionField.NUM_BITS, this.dirField.toBits());
			return flagWalker.getAllBits();
		}
		public int getId() {
			return this.idField.value();
		}
		public Direction getDirection() {
			return this.dirField.value();
		}
	}
//	
//	public static class BaseSightedFlag {
//		public static BaseSightedFlag decode(int rawFlag) {
//			
//		}
//		public int encode() {
//			
//		}
//	}
}
