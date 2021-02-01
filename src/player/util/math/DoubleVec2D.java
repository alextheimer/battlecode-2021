package player.util.math;

import java.util.Objects;

public class DoubleVec2D {
	
	public final double x;
	public final double y;
	
	/**
	 * Immutable struct-like class for storage of 2D double (x, y) vector.
	 */
	public DoubleVec2D(final double x, final double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns a *new instance* with negated dimensions.
	 */
	public DoubleVec2D negate() {
		return new DoubleVec2D(-this.x, -this.y);
	}
	
	/**
	 * Returns the vector dot product.
	 */
	public double dot(DoubleVec2D otherVec) {
		return ((this.x * otherVec.x) + (this.y * otherVec.y));
	}
	
	/**
	 * Returns the magnitude of the vector.
	 */
	public double magnitude() {
		return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
	}
	
	/**
	 * Returns the angle (in radians) between two vectors.
	 */
	public double angle(DoubleVec2D other) {
		return Math.acos(this.dot(other) / (this.magnitude() * other.magnitude()));
	}
	
	/**
	 * Returns true iff each dimension of this vector is zero (+/- FLOAT_EPS).
	 * Note that this is faster and more accurate than checking the magnitude().
	 */
	public boolean isZero() {
		return this.equals(UtilMath.ZERO_VEC);
	}
	
	public boolean sameValue(final DoubleVec2D other) {
		return (Math.abs(this.x - other.x) < UtilMath.FLOAT_EPS) && (Math.abs(this.y - other.y) < UtilMath.FLOAT_EPS);
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