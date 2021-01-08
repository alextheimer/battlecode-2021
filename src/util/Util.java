package util;

import java.util.Objects;

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
}
