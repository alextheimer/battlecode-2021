package util;

import static org.junit.Assert.*;
import static util.UtilMath.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;



public class UtilMathTest {
	
	@Test
	public void testLine() {
		class TPPPT {
			public DoubleVec2D coord;
			public Line2D line;
			public double expectedDist;
			public TPPPT(DoubleVec2D coord, Line2D line, double expectedDist) {
				this.coord = coord;
				this.line = line;
				this.expectedDist = expectedDist;
			}
		}
		Line2D vertLineOrigin = Line2D.make(new DoubleVec2D(0, 1), new DoubleVec2D(0, 0));
		Line2D horizLineOrigin = Line2D.make(new DoubleVec2D(1, 0), new DoubleVec2D(0, 0));
		Line2D yEqXLineOrigin = Line2D.make(new DoubleVec2D(1, 1), new DoubleVec2D(0, 0));
		
		List<TPPPT> listThing = Arrays.asList(
				new TPPPT(new DoubleVec2D(0, 0), vertLineOrigin, 0.0),
				new TPPPT(new DoubleVec2D(0, 0), horizLineOrigin, 0.0),
				new TPPPT(new DoubleVec2D(0, 1), horizLineOrigin, 1.0),
				new TPPPT(new DoubleVec2D(0, 1), vertLineOrigin, 0.0),
				new TPPPT(new DoubleVec2D(1, 0), vertLineOrigin, 1.0),
				new TPPPT(new DoubleVec2D(1, -1), yEqXLineOrigin, Math.sqrt(2))
				);
		for (TPPPT t : listThing) {
			assertEquals("" + t.line + ", " + t.coord, t.expectedDist, distanceFromLine(t.coord, t.line), UtilMath.FLOAT_EPS);
		}
	}

}