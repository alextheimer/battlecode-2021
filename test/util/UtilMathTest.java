package util;

import static org.junit.Assert.*;
import static player.util.math.UtilMath.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import player.util.math.DoubleVec2D;
import player.util.math.Line2D;



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
		Line2D vertLineOrigin = new Line2D(new DoubleVec2D(0, 1), new DoubleVec2D(0, 0));
		Line2D horizLineOrigin = new Line2D(new DoubleVec2D(1, 0), new DoubleVec2D(0, 0));
		Line2D yEqXLineOrigin = new Line2D(new DoubleVec2D(1, 1), new DoubleVec2D(0, 0));
		
		List<TPPPT> listThing = Arrays.asList(
				new TPPPT(new DoubleVec2D(0, 0), vertLineOrigin, 0.0),
				new TPPPT(new DoubleVec2D(0, 0), horizLineOrigin, 0.0),
				new TPPPT(new DoubleVec2D(0, 1), horizLineOrigin, 1.0),
				new TPPPT(new DoubleVec2D(0, 1), vertLineOrigin, 0.0),
				new TPPPT(new DoubleVec2D(1, 0), vertLineOrigin, 1.0),
				new TPPPT(new DoubleVec2D(1, -1), yEqXLineOrigin, Math.sqrt(2))
				);
		for (TPPPT t : listThing) {
			assertEquals("" + t.line + ", " + t.coord, t.expectedDist, distanceFromLine(t.coord, t.line), FLOAT_EPS);
		}
	}

}
