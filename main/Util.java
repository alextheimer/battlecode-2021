package main;

import java.util.Objects;

/**
 * Contains utility functions/classes.
 */
public class Util {
	/**
	 * Immutable struct-like class for storage of 2D (x, y) coordinate.
	 */
	public static class IntCoord {
		public final int x, y;
		public IntCoord(final int x, final int y) {
			this.x = x;
			this.y = y;
		}
		public boolean sameValue(final IntCoord other) {
			return (this.x == other.x) && (this.y == other.y);
		}
		@Override
		public boolean equals(final Object other) {
			return (other instanceof IntCoord) && (this.sameValue((IntCoord)other));
		}
		@Override
		public int hashCode() {
			return Objects.hash(this.x, this.y);
		}
	}
}
