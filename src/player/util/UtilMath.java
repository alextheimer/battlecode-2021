package player.util;

import java.util.Arrays;
import java.util.Objects;

import player.util.UtilMath.*;
import java.lang.Math.*;

/**
 * Stores math-related classes/functions.
 */
public class UtilMath {
	
	public static final double FLOAT_EPS = 1e-5;  // default epsilon for floating-point values
	public static final int CIRCLE_DEGREES = 360;  // degrees in a circle
	
	public static class IntVec2D {
		
		public final int x;
		public final int y;
	
		/**
		 * Immutable struct-like class for storage of 2D integer (x, y) vector.
		 */
		public IntVec2D(final int x, final int y) {
			this.x = x;
			this.y = y;
		}
		
		public IntVec2D add(IntVec2D other) {
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
	
	public static class DoubleVec2D {
		
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
		
		public boolean sameValue(final DoubleVec2D other) {
			return (Math.abs(this.x - other.x) < FLOAT_EPS) && (Math.abs(this.y - other.y) < FLOAT_EPS);
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
	
	public static class Line2D {
		
		// Each coefficient of ax + by + c = 0;
		public final double a;
		public final double b;
		public final double c;
		
		/**
		 * Immutable struct-like class for the storage of the coefficients in:
		 *     ax + by + c = 0
		 * (i.e. the equation of a line).
		 */
		public Line2D(double a, double b, double c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
		
		/**
		 * See {@link Line2D#Line2D(double, double, double)}
		 * 
		 * @param parallelTo the instantiated line will run parallel to this vector.
		 * @param onLine any point on the line
		 */
		public Line2D(DoubleVec2D parallelTo, DoubleVec2D onLine) {
			assert !parallelTo.equals(new DoubleVec2D(0.0, 0.0)) : "parallelTo must have positive magnitude";
			
			// the below assignments follow from solving: y = mx + b
			if (doubleEquals(0.0, parallelTo.x)) {
				// prevents division by zero; parallelTo is vertical.
				this.a = 1.0;
				this.b = 0.0;
				this.c = -onLine.x;
			} else {
				this.a = -(parallelTo.y / parallelTo.x);
				this.b = 1;
				this.c = (parallelTo.y * onLine.x / parallelTo.x) - onLine.y;
			}
		}
		
		@Override
		public String toString() {
			return "(a:" + this.a + ", b:" + this.b + ", c:" + this.c + ")";
		}
	}
	
	// TODO(theimer): doom fast sqrt divide!!! or just cache sqrt(a2 + b2) :(
	public static double distanceFromLine(DoubleVec2D vec, Line2D line) {
		double numerator = Math.abs((line.a * vec.x) + (line.b * vec.y) + line.c);
		double denominator = Math.sqrt(Math.pow(line.a, 2) + Math.pow(line.b, 2));
		return (numerator / denominator);
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
		int trailing = Integer.numberOfTrailingZeros(highest);
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
		return log2Ceil(numValues);
	}
	
	public static DoubleVec2D degreesToVec(int degrees) {
		assert degrees >= 0;
		assert degrees < 360;
		double radians = (Math.PI / 180) * degrees;
		double x = Math.cos(radians);
		double y = Math.sin(radians);
		return new DoubleVec2D(x, y);
	}
	
	public static double euclidianDistance(DoubleVec2D coordA, DoubleVec2D coordB) {
		return Math.sqrt(Math.pow(coordA.x - coordB.x, 2) + Math.pow(coordA.y - coordB.y, 2));
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
	
	public static boolean doubleEquals(double d1, double d2) {
		return Math.abs(d1 - d2) < FLOAT_EPS;
	}
	
	public static double vecToDegrees(DoubleVec2D vec) {
		return Math.toDegrees(Math.atan2(vec.y, vec.x));
	}
	
	public static int diffMod(int subFrom, int subThis, int mod) {
		assert subFrom >= 0;
		assert subThis >= 0;
		assert mod > subFrom;
		assert mod > subThis;
		
		int directDiff = subFrom - subThis;
		int aroundDiff = (mod - Math.abs(directDiff)) * ((directDiff >= 0) ? -1 : 1);
		
		assert (Math.abs(directDiff) + Math.abs(aroundDiff)) == mod;
		
		return (Math.abs(directDiff) < Math.abs(aroundDiff)) ? directDiff : aroundDiff;
	}
}
