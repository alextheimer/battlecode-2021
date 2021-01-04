package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import main.Search;
import main.Util.IntCoord;

class SearchTest {

	/**
	 * ~~~ Test Partitions ~~~
	 * aStar:
	 *     expand
	 *         -sometimes bidirectional, never bidirectional
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
	static boolean intCoordInBounds(final IntCoord coord, final int xMin, final int xMax,
			                 final int yMin, final int yMax) {
		return ((coord.x >= xMin) && (coord.x < xMax) &&
			    (coord.y >= yMin) && (coord.y < yMax));
	}

	static boolean pathIsConnected(final List<IntCoord> path, final Function<IntCoord, Set<IntCoord>> expandFunc) {
		for (int i = 0; i < (path.size() - 1); ++i) {
			final Set<IntCoord> adjacentSet = expandFunc.apply(path.get(i));
			if (!adjacentSet.contains(path.get(i+1))) {
				return false;
			}
		}
		return true;
	}

	// isEndgameCheck ------------------------------------------------

	/**
	 * Returns a Predicate that returns true if and only if its argument equals() goal.
	 */
	static Predicate<IntCoord> makeEndgamePred(final IntCoord goal) {
		return coord -> goal.equals(coord);
	}

	// cost ----------------------------------------------------------

	/**
	 * Returns the Euclidian distance between the argument IntCoords.
	 */
	static double cost(final IntCoord coordA, final IntCoord coordB) {
		return Math.sqrt(Math.pow(coordA.x - coordB.x, 2) + (Math.pow(coordA.y - coordB.y, 2)));
	}

	// heuristic -----------------------------------------------------

	/**
	 * Returns a Function that returns the Euclidian distance of an IntCoord from goal.
	 */
	static Function<IntCoord, Double> makeHeuristicFunc(final IntCoord goal) {
		return coord -> cost(coord, goal);
	}

	// expand --------------------------------------------------------

	/**
	 * Returns a Function that expands an IntCoord into its neighbors. TODO(theimer): explain.
	 */
	static Function<IntCoord, Set<IntCoord>> makeExpandFunc(final int xMin, final int xMax,
			                                                final int yMin, final int yMax,
			                                                final BiPredicate<IntCoord, IntCoord> intCoordFilter) {
		return new Function<IntCoord, Set<IntCoord>>() {
			@Override
			public Set<IntCoord> apply(final IntCoord coord) {
				assert intCoordInBounds(coord, xMin, xMax, yMin, yMax);  // TODO(theimer): message
				// TODO(theimer): make this faster if speed matters.
				return Stream.of(
					new IntCoord(coord.x, coord.y + 1),
					new IntCoord(coord.x, coord.y - 1),
					new IntCoord(coord.x + 1, coord.y),
					new IntCoord(coord.x + 1, coord.y + 1),
					new IntCoord(coord.x + 1, coord.y - 1),
					new IntCoord(coord.x - 1, coord.y),
					new IntCoord(coord.x - 1, coord.y + 1),
					new IntCoord(coord.x - 1, coord.y - 1)
				).filter(expandedCoord -> (intCoordInBounds(expandedCoord, xMin, xMax, yMin, yMax) &&
						                   intCoordFilter.test(coord, expandedCoord)))
			     .collect(Collectors.toSet());
			}
		};
	}

	/*==== Unit Tests ==========================================================================*/

	/**
	 * Covers:
     *     expand- sometimes bidirectional, returns only nonempty sets
     *     result- length > 1, valid path
	 */
	@Test
	void aStarLongBidirectional() {
		final int xMin = 0;
		final int xMax = 10;
		final int yMin = 0;
		final int yMax = 10;
		final IntCoord startCoord = new IntCoord(xMin, yMin);
		final IntCoord goalCoord = new IntCoord(xMax/2, yMax/2);
		final Predicate<IntCoord> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntCoord, IntCoord> allFilter = (coord, expandedCoord) -> true;
		final Function<IntCoord, Set<IntCoord>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, allFilter);
		final BiFunction<IntCoord, IntCoord, Double> cost = SearchTest::cost;
		final Function<IntCoord, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntCoord> result = Search.aStar(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertTrue(result.size() > 1);
		assertEquals(startCoord, result.get(0), String.format(
				"startCoord: (%d, %d), result.get(0): (%d, %d)",
				startCoord.x, startCoord.y, result.get(0).x, result.get(0).y));
		assertEquals(goalCoord, result.get(result.size()-1));
		assertTrue(pathIsConnected(result, expand));
	}

	/**
	 * Covers:
     *     expand- never bidirectional
	 */
	@Test
	void aStarOneWay() {
		final int xMin = 0;
		final int xMax = 10;
		final int yMin = 0;
		final int yMax = 10;
		final IntCoord startCoord = new IntCoord(xMin, yMin);
		final IntCoord goalCoord = new IntCoord(xMax/2, yMax/2);
		final Predicate<IntCoord> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntCoord, IntCoord> upRightFilter =
				(coord, expandedCoord) -> (expandedCoord.x >= coord.x) && (expandedCoord.y >= coord.y);
		final Function<IntCoord, Set<IntCoord>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, upRightFilter);
		final BiFunction<IntCoord, IntCoord, Double> cost = SearchTest::cost;
		final Function<IntCoord, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntCoord> result = Search.aStar(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertTrue(result.size() > 1);
		assertEquals(startCoord, result.get(0), String.format(
				"startCoord: (%d, %d), result.get(0): (%d, %d)",
				startCoord.x, startCoord.y, result.get(0).x, result.get(0).y));
		assertEquals(goalCoord, result.get(result.size()-1));
		assertTrue(pathIsConnected(result, expand));
	}

	/**
	 * Covers:
     *     return- no valid path
	 */
	@Test
	void aStarNoPath() {
		final int xMin = 0;
		final int xMax = 10;
		final int yMin = 0;
		final int yMax = 10;
		final IntCoord startCoord = new IntCoord(xMin, yMin);
		final IntCoord goalCoord = new IntCoord(xMax, yMax);
		final Predicate<IntCoord> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntCoord, IntCoord> lineObstacleFilter =
				(coord, expandedCoord) -> (expandedCoord.x != (xMax / 2));
		final Function<IntCoord, Set<IntCoord>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, lineObstacleFilter);
		final BiFunction<IntCoord, IntCoord, Double> cost = SearchTest::cost;
		final Function<IntCoord, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntCoord> result = Search.aStar(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertEquals(result, List.of());  // TODO(theimer): messages
	}

	/**
	 * Covers:
     *     return- length == 1
	 */
	@Test
	void aStarStartInEndgame() {
		final int xMin = 0;
		final int xMax = 10;
		final int yMin = 0;
		final int yMax = 10;
		final IntCoord startCoord = new IntCoord(xMin, yMin);
		final IntCoord goalCoord = new IntCoord(xMin, yMin);
		final Predicate<IntCoord> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntCoord, IntCoord> allFilter = (coord, expandedCoord) -> true;
		final Function<IntCoord, Set<IntCoord>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, allFilter);
		final BiFunction<IntCoord, IntCoord, Double> cost = SearchTest::cost;
		final Function<IntCoord, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntCoord> result = Search.aStar(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertEquals(startCoord, goalCoord);  // sanity check
		assertEquals(result, List.of(startCoord));
	}

	/**
	 * Covers:
     *     expand- returns empty set during search
	 */
	@Test
	void aStarExpandReturnsEmptySet() {
		final int xMin = 0;
		final int xMax = 5;
		final int yMin = 0;
		final int yMax = 5;
		final IntCoord startCoord = new IntCoord(xMin, yMin);
		final IntCoord goalCoord = new IntCoord(xMax - 1, yMax - 1);
		final Predicate<IntCoord> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntCoord, IntCoord> edgeFilter =
				(coord, expandedCoord) -> (coord.y == yMin) || (coord.x == (xMax - 1));
		final Function<IntCoord, Set<IntCoord>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, edgeFilter);
		final BiFunction<IntCoord, IntCoord, Double> cost = SearchTest::cost;
		final Function<IntCoord, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntCoord> result = Search.aStar(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertTrue(result.size() > 1);
		assertEquals(startCoord, result.get(0), String.format(
				"startCoord: (%d, %d), result.get(0): (%d, %d)",
				startCoord.x, startCoord.y, result.get(0).x, result.get(0).y));
		assertEquals(goalCoord, result.get(result.size()-1));
		assertTrue(pathIsConnected(result, expand));
	}

}
