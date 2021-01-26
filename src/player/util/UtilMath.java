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
	public static final DoubleVec2D ZERO_VEC = new DoubleVec2D(0.0, 0.0);
	
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
		
		/**
		 * Returns the magnitude of the vector.
		 */
		public double magnitude() {
			return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
		}
		
		/**
		 * Returns true iff each dimension of this vector is zero (+/- FLOAT_EPS).
		 * Note that this is faster and more accurate than checking the magnitude().
		 */
		public boolean isZero() {
			return this.equals(ZERO_VEC);
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
		 * 
		 * @throws RuntimeException if the arguments do not define a line.
		 */
		public Line2D(double a, double b, double c) {
			if (doubleEquals(0.0, a) && doubleEquals(0.0, b)) {
				throw new RuntimeException(String.format("arguments do not define a line: a:%f b:%f c:%f", a, b, b));
			}
			
			this.a = a;
			this.b = b;
			this.c = c;
		}
		
		/**
		 * See {@link Line2D#Line2D(double, double, double)}
		 * 
		 * @param parallelTo the instantiated line will run parallel to this vector.
		 *     Must have positive magnitude.
		 * @param onLine any point on the line
		 */
		public Line2D(DoubleVec2D parallelTo, DoubleVec2D onLine) {
			assert !parallelTo.isZero() : "parallelTo must have positive magnitude: " + parallelTo;
			
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
	
	/**
	 * Returns the minimum distance between a coordinate and a line on the Euclidian plane.
	 * @param coord an (x,y) coordinate
	 * @param line the line to measure distance from
	 */
	public static double distanceFromLine(DoubleVec2D coord, Line2D line) {
		// TODO(theimer): doom fast sqrt divide!!! or just cache sqrt(a**2 + b**2) :(
		
		// the following math is straight from wikipedia
		double numerator = Math.abs((line.a * coord.x) + (line.b * coord.y) + line.c);
		double denominator = Math.sqrt(Math.pow(line.a, 2) + Math.pow(line.b, 2));
		return (numerator / denominator);
	}
	
	/**
	 * Returns the floor of log2 on a positive integer argument.
	 * @param val must be greater than zero
	 */
	public static int log2Floor(int val) {
		assert val > 0 : "val must be greater than zero: " + val;
		int highestBit = Integer.highestOneBit(val);
		return Integer.numberOfTrailingZeros(highestBit);
	}
	
	/**
	 * Returns the ceiling of log2 on a positive integer argument.
	 * @param val must be greater than zero
	 */
	public static int log2Ceil(int val) {
		assert val > 0 : "val must be greater than zero: " + val;
		int highestBit = Integer.highestOneBit(val);
		int numTrailingZeros = Integer.numberOfTrailingZeros(highestBit);
		if (highestBit == val) {
			// val is a power of two!
			return numTrailingZeros;
		} else {
			// val is not an exact power of two; round up.
			return numTrailingZeros + 1;
		}
	}
	
	/**
	 * Returns true iff `val` is an exact power of two; else false.
	 * @param val must be greater than zero
	 */
	public static boolean isPow2(int val) {
		assert val > 0 : "val must be greater than zero: " + val;
		return (val != 0) && ((val & (val - 1)) == 0);
	}
	
	/**
	 * Converts an angle to a unit vector.
	 * @param degrees angle counter-clockwise from the x-axis.
	 *     Must lie on [0, CIRCLE_DEGREES).
	 * @return a unit vector oriented in the direction of `degrees`.
	 */
	public static DoubleVec2D degreesToVec(int degrees) {
		// Could just use (degrees % CIRCLE_DEGREES), but this will probably catch more errors.
		assert degrees >= 0 : "degrees must be at least zero: " + degrees;
		assert degrees < CIRCLE_DEGREES : "degrees must be less than CIRCLE_DEGREES: " + degrees;
		
		double radians = (2 * Math.PI / CIRCLE_DEGREES) * degrees;
		double x = Math.cos(radians);
		double y = Math.sin(radians);
		return new DoubleVec2D(x, y);
	}
	
	/**
	 * Converts a vector to an angle.
	 * @param vec must have magnitude > 0
	 * @return the angle (in degrees) at which `vec` is rotated counterclockwise
	 *     from the x-axis.
	 */
	public static double vecToDegrees(DoubleVec2D vec) {
		assert !vec.isZero(): "vec must have positive magnitude: " + vec;
		return Math.toDegrees(Math.atan2(vec.y, vec.x));
	}
	
	/**
	 * Returns the Euclidian distance between two coordinates.
	 */
	public static double euclidianDistance(DoubleVec2D coordA, DoubleVec2D coordB) {
		// This is a static function because distance doesn't mean much to a raw vector
		// unless it's interpreted as a coordinate.
		return Math.sqrt(Math.pow(coordA.x - coordB.x, 2) + Math.pow(coordA.y - coordB.y, 2));
	}
	
	/**
	 * Returns true iff `coord` lies within the space defined by:
	 *     {coord.x on [xMin, xMax) && coord.y on [yMin, yMax)}
	 *     
	 * @param xMin must be < xMax
	 * @param xMax must be > xMin
	 * @param yMin must be < yMax
	 * @param yMax must be > yMin
	 */
	public static boolean intCoordInBounds(final IntVec2D coord, final int xMin, final int xMax,
			                               final int yMin, final int yMax) {
		assert (xMin < xMax) && (yMin < yMax) :
			String.format("invalid bounds: (xMin:%d, xMax:%d), (yMin:%d, yMax:%d)", xMin, xMax, yMin, yMax);
		return ((coord.x >= xMin) && (coord.x < xMax) &&
			    (coord.y >= yMin) && (coord.y < yMax));
	}
	
	/**
	 * Returns true iff the absolute difference between the arguments is less than FLOAT_EPS.
	 */
	public static boolean doubleEquals(double d1, double d2) {
		return Math.abs(d1 - d2) < FLOAT_EPS;
	}
	
	/**
	 * Given the *circular* range [0, mod) and two values valFrom, valTo on that range, returns
	 * the delta such that:
	 *     1) valFrom + delta = valTo, and
	 *     2) abs(delta) is minimal
	 *     
	 * Example:
	 * 
	 *   L1                 L2                  L3
	 * -----< >----------------------------> <------
	 * |-----|-----------------------------|-------|
	 * 0  valFrom                        valTo    mod
	 * 
	 * There are two directions one might travel from valFrom two valTo: "directly" or through zero.
	 * The value -(L1 + L3) would be returned in this example because its magnitude is less than L2.
	 */
	public static int diffMod(int valTo, int valFrom, int mod) {
		assert (valTo >= 0) && (valTo < mod) :
			String.format("valTo must lie on [0, mod): valTo:%d, mod:%d", valTo, mod);
		assert (valFrom >= 0) && (valFrom < mod) :
			String.format("valFrom must lie on [0, mod): valTo:%d, mod:%d", valFrom, mod);
		
		int directDiff = valTo - valFrom;
		// Note that this will always have the opposite sign of directDiff.
		int aroundDiff = (mod - Math.abs(directDiff)) * ((directDiff >= 0) ? -1 : 1);
		
		return (Math.abs(directDiff) < Math.abs(aroundDiff)) ? directDiff : aroundDiff;
	}
}
