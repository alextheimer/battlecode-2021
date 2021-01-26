package player.util.math;

public class Line2D {
	
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
		if (UtilMath.doubleEquals(0.0, a) && UtilMath.doubleEquals(0.0, b)) {
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
		if (UtilMath.doubleEquals(0.0, parallelTo.x)) {
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