package player.util.math;

import java.util.Objects;

public class IntVec2D {

	public final int x;
	public final int y;

	/**
	 * Immutable struct-like class for storage of 2D integer (x, y) vector.
	 */
	public IntVec2D(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public IntVec2D add(final IntVec2D other) {
		return new IntVec2D(this.x + other.x, this.y + other.y);
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