package main;

/**
 * Contains utility functions/classes.
 */
public class Util {
	/**
	 * Immutable struct-like class for storage of 2D (x, y) coordinate.
	 */
	public class IntCoord {
		public final int x, y;
		public IntCoord(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
