package util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import player.util.math.DoubleVec2D;
import player.util.math.Line2D;
import player.util.math.UtilMath;

public class Line2DTest {

	/**
	 * ~~~ Test Partitions ~~~
	 *
	 * distanceFromLine:
	 *     coord
	 *         -origin, non-origin
	 *         -(non-vertical) above line, below line, on line
	 *         -(vertical) left of line, right of line, on line
	 *     line
	 *         -horizontal, vertical, neither
	 *         -thru origin, not thru origin
	 *     result
	 *         -zero, positive
	 */

	/**
	 * Covers all partitions.
	 */
	@Test
	public void testLine() {
		class TestStruct {
			// just stores a line, coordinate, and the coordinate's expected distance from the line.
			public DoubleVec2D coord;
			public Line2D line;
			public double expectedDist;
			public TestStruct(final DoubleVec2D coord, final Line2D line, final double expectedDist) {
				this.coord = coord;
				this.line = line;
				this.expectedDist = expectedDist;
			}
		}

		// vertical / horizontal / y=x lines thru the origin
		final Line2D vertLineOrigin = new Line2D(new DoubleVec2D(0, 1), new DoubleVec2D(0, 0));
		final Line2D horizLineOrigin = new Line2D(new DoubleVec2D(1, 0), new DoubleVec2D(0, 0));
		final Line2D yEqXLineOrigin = new Line2D(new DoubleVec2D(1, 1), new DoubleVec2D(0, 0));

		final DoubleVec2D originCoord = new DoubleVec2D(0, 0);

		final List<TestStruct> testList = Arrays.asList(
				// origin coord, vert line, line thru origin, coord on line, result 0
				new TestStruct(originCoord,            vertLineOrigin,  0.0),
				// origin coord, horiz line, line thru origin, coord on line, result 0
				new TestStruct(originCoord,            horizLineOrigin, 0.0),
				// non-origin coord, horiz line, line thru origin, coord above line, result nonzero
				new TestStruct(new DoubleVec2D(0, 1),  horizLineOrigin, 1.0),
				// non-origin coord, vert line, line thru origin, coord on line, result 0
				new TestStruct(new DoubleVec2D(0, 1),  vertLineOrigin,  0.0),
				// non-origin coord, vert line, line thru origin, coord right of line, result nonzero
				new TestStruct(new DoubleVec2D(1, 0),  vertLineOrigin,  1.0),
				// non-origin coord, vert line, line thru origin, coord left of line, result nonzero
				new TestStruct(new DoubleVec2D(-1, 0),  vertLineOrigin,  1.0),
				// origin coord, sloped line, line not thru origin, coord below line, result nonzero
				new TestStruct(originCoord,
						// y = x + 1
						new Line2D(new DoubleVec2D(1, 1), new DoubleVec2D(0, 1)),
						Math.sqrt(2)/2),
				// non-origin coord, sloped line, line thru origin, coord below line, result nonzero
				new TestStruct(new DoubleVec2D(1, -1), yEqXLineOrigin,  Math.sqrt(2))
		);

		// compare each actual vs expected result
		for (final TestStruct t : testList) {
			Assert.assertEquals(
					"distance mismatch; " + t.coord + ", " + t.line,
					t.expectedDist,
					UtilMath.distanceFromLine(t.coord, t.line),
					UtilMath.FLOAT_EPS);
		}
	}

}
