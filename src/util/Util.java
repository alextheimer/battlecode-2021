package util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import battlecode.common.*;

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
	
	public static class PeekableIteratorWrapper<T> implements Iterator<T> {
		private Iterator<T> iterator;
		private Optional<T> peekedOptional;
		public PeekableIteratorWrapper(Iterator<T> iterator) {
			this.iterator = iterator;
			this.peekedOptional = Optional.empty();
		}
		public boolean hasNext() {
			return (this.peekedOptional.isPresent() || this.iterator.hasNext());
		}
		public T next() {
			if (this.peekedOptional.isPresent()) {
				T nextElt = this.peekedOptional.get();
				this.peekedOptional = Optional.empty();
				return nextElt;
				
			} else {
				return this.iterator.next();
			}
		}
		public T peek() {
			if (!this.peekedOptional.isPresent()) {
				this.peekedOptional = Optional.of(this.iterator.next());
			}
			return this.peekedOptional.get();
		}
	}
	
	public static class Line2D {
		public double a;
		public double b;
		public double c;
		
		public Line2D(double a, double b, double c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
		
		public static Line2D make(DoubleVec2D vec, DoubleVec2D origin) {
			double a = 1;  // TODO(theimer): could probably get rid of this; currently just here for completeness.
			double b = -(vec.y / vec.x);
			double c = -((vec.x * origin.y) / (vec.y * origin.x));
			return new Line2D(a, b, c);
		}
	}
	
	// TODO(theimer): doom fast sqrt divide!!! or just cache sqrt(a2 + b2) :(
	public static double distanceFromLine(DoubleVec2D vec, Line2D line) {
		double numerator = Math.abs((line.a * vec.x) + (line.b * vec.y) + line.c);
		double denominator = Math.sqrt(Math.pow(line.a, 2) + Math.pow(line.b, 2));
		return (numerator / denominator);
	}
	
	public static <T> T findLeastCostLinear(Iterable<T> iterable, Function<T, Double> costFunc) {
		Iterator<T> iterator = iterable.iterator();
		assert iterator.hasNext();
		T leastCostElt = iterator.next();
		double leastCost = costFunc.apply(leastCostElt);
		while (iterator.hasNext()) {
			T nextElt = iterator.next();
			double nextEltCost = costFunc.apply(nextElt);
			if (nextEltCost < leastCost) {
				leastCostElt = nextElt;
				leastCost = nextEltCost;
			}
		}
		return leastCostElt;
		
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
	
	public static DoubleVec2D degreesToVec(int degrees) {
		assert degrees >= 0 && degrees < 360;
		double radians = (Math.PI / 180) * degrees;
		double x = Math.cos(radians);
		double y = Math.sin(radians);
		return new DoubleVec2D(x, y);
	}
	
	public static double euclidianDistance(DoubleVec2D coordA, DoubleVec2D coordB) {
		return Math.sqrt(Math.pow(coordA.x - coordB.x, 2) + Math.pow(coordA.y - coordB.y, 2));
	}
	
	public static Direction directionToGoal(DoubleVec2D startCoord, DoubleVec2D goalCoord) {
		// TODO(theimer): literally anything better than this
		if (goalCoord.x > startCoord.x) {
			if (goalCoord.y > startCoord.y) {
				return Direction.NORTHEAST;
			} else {
				return Direction.SOUTHEAST;
			}
		} else {
			if (goalCoord.y > startCoord.y) {
				return Direction.NORTHWEST;
			} else {
				return Direction.SOUTHWEST;
			}			
		}
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
