package test;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import main.Util.IntCoord;

class SearchTest {

	/**
	 * ~~~ Test Partitions ~~~
	 * aStar:
	 *     cost
	 *         -returns 0 at least once during search, never returns 0 during search
	 *     expand
	 *         -only bidirectional, both bidirectional and one-way
	 *         -gives self-loops during search; gives no self-loops
	 *         -returns empty set during search; returns only nonempty sets
	 *     result
	 *         -no valid path, valid path
	 *         -result is length 1 (start == end), result > length 1
	 *     TODO(theimer): endgame is one node; endgame encompasses multiple nodes
	 */

	/*==== Common Functions ====================================================================*/

	// Utils ---------------------------------------------------------

	/**
	 * Returns true if coord lies within the space defined by:
	 *     {coord.x on [xMin, xMax) && coord.y on [yMin, yMax)}
	 */
	boolean intCoordInBounds(final IntCoord coord, final int xMin, final int xMax,
			                 final int yMin, final int yMax) {
		return ((coord.x >= xMin) && (coord.x < xMax) &&
			    (coord.y >= yMin) && (coord.y < yMax));
	}

	// isEndgameCheck ------------------------------------------------

	/**
	 * Returns a Predicate that returns true if and only if its argument equals() goal.
	 */
	Predicate<IntCoord> makeEndgamePred(final IntCoord goal) {
		return coord -> goal.equals(coord);
	}

	// cost ----------------------------------------------------------

	/**
	 * Returns the Euclidian distance between the argument IntCoords.
	 */
	double cost(final IntCoord coordA, final IntCoord coordB) {
		return Math.sqrt(Math.pow(coordA.x - coordB.x, 2) + (Math.pow(coordA.y - coordB.y, 2)));
	}

	// heuristic -----------------------------------------------------

	/**
	 * Returns a Function that returns the Euclidian distance of an IntCoord from goal.
	 */
	Function<IntCoord, Double> makeHeuristicFunc(final IntCoord goal) {
		return coord -> cost(coord, goal);
	}

	// expand --------------------------------------------------------

	/**
	 * Returns a Function that expands an IntCoord into its cardinal neighbors. TODO(theimer): explain.
	 */
	Function<IntCoord, Set<IntCoord>> makeExpandFuncCardinal(final int xMin, final int xMax,
			                                                 final int yMin, final int yMax) {
		return new Function<IntCoord, Set<IntCoord>>() {
			@Override
			public Set<IntCoord> apply(final IntCoord coord) {
				assert intCoordInBounds(coord, xMin, xMax, yMin, yMax);  // TODO(theimer): message
				// TODO(theimer): make this faster if speed matters.
				return Stream.of(
					new IntCoord(coord.x + 1, coord.y),
					new IntCoord(coord.x - 1, coord.y),
					new IntCoord(coord.x, coord.y + 1),
					new IntCoord(coord.x, coord.y - 1)
				).filter(expandedCoord -> intCoordInBounds(expandedCoord, xMin, xMax, yMin, yMax))
			     .collect(Collectors.toSet());
			}
		};
	}

	/*==== Unit Tests ==========================================================================*/

	/**
	 * Covers:
     *     cost- returns 0 at least once during search
     *     expand- only bidirectional, self-loops, returns only nonempty sets
     *     result- length > 1, valid path
	 */
	@Test
	void aStarCostReturnsZeroAndBidirectionalWithSelfLoops() {
		fail("Not yet implemented");
	}

	/**
	 * Covers:
     *     cost- never returns 0 during search
     *     expand- bidirectional and one-way, no self loops
	 */
	@Test
	void aStarBidirectionalAndOneWay() {
		fail("Not yet implemented");
	}

	/**
	 * Covers:
     *     return- no valid path
	 */
	@Test
	void aStarNoPath() {
		fail("Not yet implemented");
	}

	/**
	 * Covers:
     *     return- length == 1
	 */
	@Test
	void aStarStartInEndgame() {
		fail("Not yet implemented");
	}

	/**
	 * Covers:
     *     expand- returns empty set during search
	 */
	@Test
	void aStarExpandReturnsEmptySet() {
		fail("Not yet implemented");
	}

}
