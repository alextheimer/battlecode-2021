package player.util.battlecode.flag;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;

import battlecode.common.Direction;
import battlecode.common.RobotType;
import player.util.battlecode.flag.FlagFields.CoordField;
import player.util.battlecode.flag.FlagFields.DegreesField;
import player.util.battlecode.flag.FlagFields.DiffVecField;
import player.util.battlecode.flag.FlagFields.DirectionField;
import player.util.battlecode.flag.FlagFields.IdField;
import player.util.battlecode.flag.FlagFields.RobotTypeField;
import player.util.math.IntVec2D;
import player.util.math.UtilMath;

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
	
	public static class RobotTypeField {
		public static final int NUM_BITS = UtilMath.log2Ceil(RobotType.values().length);
		
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