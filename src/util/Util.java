package util;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Contains utility functions/classes.
 */
public class Util {	
	/**
	 * Immutable struct-like class for storage of 2D integer (x, y) vector.
	 */
	public static class IntVec2D {
		public final int x, y;
		public IntVec2D(final int x, final int y) {
			this.x = x;
			this.y = y;
		}
		public boolean sameValue(final IntVec2D other) {
			return (this.x == other.x) && (this.y == other.y);
		}
		@Override
		public boolean equals(final Object other) {
			return (other instanceof IntVec2D) && (this.sameValue((IntVec2D)other));
		}
		@Override
		public int hashCode() {
			return Objects.hash(this.x, this.y);
		}
		@Override
		public String toString() {
			return "(" + this.x + ", " + this.y + ")";
		}
	}
	
	/**
	 * Immutable struct-like class for storage of 2D double (x, y) vector.
	 */
	public static class DoubleVec2D {
		public final double EPS = 1e-5;
		public final double x, y;
		public DoubleVec2D(final double x, final double y) {
			this.x = x;
			this.y = y;
		}
		public boolean sameValue(final DoubleVec2D other) {
			return (Math.abs(this.x - other.x) < EPS) && (Math.abs(this.y - other.y) < EPS);
		}
		@Override
		public boolean equals(final Object other) {
			return (other instanceof DoubleVec2D) && (this.sameValue((DoubleVec2D)other));
		}
		@Override
		public int hashCode() {
			return Objects.hash(this.x, this.y);
		}
		@Override
		public String toString() {
			return "(" + this.x + ", " + this.y + ")";
		}
	}
	
	// TODO(theimer): more Java-thonic way to do this?
	public static int log2Floor(int val) {
		assert val > 0 : "val must be at least zero!";
		int highest = Integer.highestOneBit(val);
		return Integer.numberOfTrailingZeros(highest);
	}
	
	// TODO(theimer): more Java-thonic way to do this?
	public static int log2Ceil(int val) {
		assert val > 0 : "val must be at least zero!";
		int highest = Integer.highestOneBit(val);
		int trailing = Integer.numberOfTrailingZeros(val);
		if (highest == val) {
			// Power of two!
			return trailing;
		} else {
			// TODO(theimer): asserts
			return trailing + 1;
		}
	}
	
	public static boolean isPow2(int val) {
		return (val != 0) && ((val & (val - 1)) == 0);
	}
	
	public static int numBits(int numValues) {
		return 1 << log2Ceil(numValues);
	}
	
	public static Stream<IntVec2D> makeAllAdjacentStream(IntVec2D coord) {
		return Stream.of(
				new IntVec2D(coord.x, coord.y + 1),
				new IntVec2D(coord.x, coord.y - 1),
				new IntVec2D(coord.x + 1, coord.y),
				new IntVec2D(coord.x + 1, coord.y + 1),
				new IntVec2D(coord.x + 1, coord.y - 1),
				new IntVec2D(coord.x - 1, coord.y),
				new IntVec2D(coord.x - 1, coord.y + 1),
				new IntVec2D(coord.x - 1, coord.y - 1)
			);
	}
	
	/**
	 * Returns true if coord lies within the space defined by:
	 *     {coord.x on [xMin, xMax) && coord.y on [yMin, yMax)}
	 */
	public static boolean intCoordInBounds(final IntVec2D coord, final int xMin, final int xMax,
			                 final int yMin, final int yMax) {
		return ((coord.x >= xMin) && (coord.x < xMax) &&
			    (coord.y >= yMin) && (coord.y < yMax));
	}
}
