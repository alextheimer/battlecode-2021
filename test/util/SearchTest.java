package util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static player.util.math.UtilMath.intCoordInBounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import player.util.math.IntVec2D;
import player.util.search.AStar;

public class SearchTest {

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

//	/**
//	 * Returns true if coord lies within the space defined by:
//	 *     {coord.x on [xMin, xMax) && coord.y on [yMin, yMax)}
//	 */
//	static boolean intCoordInBounds(final IntVec2D coord, final int xMin, final int xMax,
//			                 final int yMin, final int yMax) {
//		return ((coord.x >= xMin) && (coord.x < xMax) &&
//			    (coord.y >= yMin) && (coord.y < yMax));
//	}

	static boolean pathIsConnected(final List<IntVec2D> path, final Function<IntVec2D, Set<IntVec2D>> expandFunc) {
		for (int i = 0; i < (path.size() - 1); ++i) {
			final Set<IntVec2D> adjacentSet = expandFunc.apply(path.get(i));
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
	static Predicate<IntVec2D> makeEndgamePred(final IntVec2D goal) {
		return coord -> goal.equals(coord);
	}

	// cost ----------------------------------------------------------

	/**
	 * Returns the Euclidian distance between the argument IntCoords.
	 */
	static double cost(final IntVec2D coordA, final IntVec2D coordB) {
		return Math.sqrt(Math.pow(coordA.x - coordB.x, 2) + (Math.pow(coordA.y - coordB.y, 2)));
	}

	// heuristic -----------------------------------------------------

	/**
	 * Returns a Function that returns the Euclidian distance of an IntCoord from goal.
	 */
	static Function<IntVec2D, Double> makeHeuristicFunc(final IntVec2D goal) {
		return coord -> cost(coord, goal);
	}

	// expand --------------------------------------------------------

	/**
	 * Returns a Function that expands an IntCoord into its neighbors. TODO(theimer): explain.
	 */
	static Function<IntVec2D, Set<IntVec2D>> makeExpandFunc(final int xMin, final int xMax,
			                                                final int yMin, final int yMax,
			                                                final BiPredicate<IntVec2D, IntVec2D> intCoordFilter) {
		return new Function<IntVec2D, Set<IntVec2D>>() {
			@Override
			public Set<IntVec2D> apply(final IntVec2D coord) {
				assert intCoordInBounds(coord, xMin, xMax, yMin, yMax) : "coord: " + coord;
				// TODO(theimer): make this faster if speed matters.
				return Stream.of(
					new IntVec2D(coord.x, coord.y + 1),
					new IntVec2D(coord.x, coord.y - 1),
					new IntVec2D(coord.x + 1, coord.y),
					new IntVec2D(coord.x + 1, coord.y + 1),
					new IntVec2D(coord.x + 1, coord.y - 1),
					new IntVec2D(coord.x - 1, coord.y),
					new IntVec2D(coord.x - 1, coord.y + 1),
					new IntVec2D(coord.x - 1, coord.y - 1)
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
	public void aStarLongBidirectional() {
		final int xMin = 0;
		final int xMax = 10;
		final int yMin = 0;
		final int yMax = 10;
		final IntVec2D startCoord = new IntVec2D(xMin, yMin);
		final IntVec2D goalCoord = new IntVec2D(xMax/2, yMax/2);
		final Predicate<IntVec2D> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntVec2D, IntVec2D> allFilter = (coord, expandedCoord) -> true;
		final Function<IntVec2D, Set<IntVec2D>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, allFilter);
		final BiFunction<IntVec2D, IntVec2D, Double> cost = SearchTest::cost;
		final Function<IntVec2D, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntVec2D> result = AStar.aStarSearch(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertTrue(result.size() > 1);
		assertEquals(
		    String.format(
				"startCoord: (%d, %d), result.get(0): (%d, %d)",
				startCoord.x, startCoord.y, result.get(0).x, result.get(0).y),
			startCoord, result.get(0));
		assertEquals(goalCoord, result.get(result.size()-1));
		assertTrue(pathIsConnected(result, expand));
	}

	/**
	 * Covers:
     *     expand- never bidirectional
	 */
	@Test
	public void aStarOneWay() {
		final int xMin = 0;
		final int xMax = 10;
		final int yMin = 0;
		final int yMax = 10;
		final IntVec2D startCoord = new IntVec2D(xMin, yMin);
		final IntVec2D goalCoord = new IntVec2D(xMax/2, yMax/2);
		final Predicate<IntVec2D> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntVec2D, IntVec2D> upRightFilter =
				(coord, expandedCoord) -> (expandedCoord.x >= coord.x) && (expandedCoord.y >= coord.y);
		final Function<IntVec2D, Set<IntVec2D>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, upRightFilter);
		final BiFunction<IntVec2D, IntVec2D, Double> cost = SearchTest::cost;
		final Function<IntVec2D, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntVec2D> result = AStar.aStarSearch(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertTrue(result.size() > 1);
		assertEquals(
		    String.format(
				"startCoord: (%d, %d), result.get(0): (%d, %d)",
				startCoord.x, startCoord.y, result.get(0).x, result.get(0).y),
			startCoord, result.get(0));
		assertEquals(goalCoord, result.get(result.size()-1));
		assertTrue(pathIsConnected(result, expand));
	}

	/**
	 * Covers:
     *     return- no valid path
	 */
	@Test
	public void aStarNoPath() {
		final int xMin = 0;
		final int xMax = 10;
		final int yMin = 0;
		final int yMax = 10;
		final IntVec2D startCoord = new IntVec2D(xMin, yMin);
		final IntVec2D goalCoord = new IntVec2D(xMax, yMax);
		final Predicate<IntVec2D> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntVec2D, IntVec2D> lineObstacleFilter =
				(coord, expandedCoord) -> (expandedCoord.x != (xMax / 2));
		final Function<IntVec2D, Set<IntVec2D>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, lineObstacleFilter);
		final BiFunction<IntVec2D, IntVec2D, Double> cost = SearchTest::cost;
		final Function<IntVec2D, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntVec2D> result = AStar.aStarSearch(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertEquals(result, Collections.emptyList());  // TODO(theimer): messages
	}

	/**
	 * Covers:
     *     return- length == 1
	 */
	@Test
	public void aStarStartInEndgame() {
		final int xMin = 0;
		final int xMax = 10;
		final int yMin = 0;
		final int yMax = 10;
		final IntVec2D startCoord = new IntVec2D(xMin, yMin);
		final IntVec2D goalCoord = new IntVec2D(xMin, yMin);
		final Predicate<IntVec2D> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntVec2D, IntVec2D> allFilter = (coord, expandedCoord) -> true;
		final Function<IntVec2D, Set<IntVec2D>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, allFilter);
		final BiFunction<IntVec2D, IntVec2D, Double> cost = SearchTest::cost;
		final Function<IntVec2D, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntVec2D> result = AStar.aStarSearch(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertEquals(startCoord, goalCoord);  // sanity check
		assertEquals(result, Arrays.asList(startCoord));
	}

	/**
	 * Covers:
     *     expand- returns empty set during search
	 */
	@Test
	public void aStarExpandReturnsEmptySet() {
		final int xMin = 0;
		final int xMax = 5;
		final int yMin = 0;
		final int yMax = 5;
		final IntVec2D startCoord = new IntVec2D(xMin, yMin);
		final IntVec2D goalCoord = new IntVec2D(xMax - 1, yMax - 1);
		final Predicate<IntVec2D> isEndgameCheck = makeEndgamePred(goalCoord);
		final BiPredicate<IntVec2D, IntVec2D> edgeFilter =
				(coord, expandedCoord) -> (coord.y == yMin) || (coord.x == (xMax - 1));
		final Function<IntVec2D, Set<IntVec2D>> expand = makeExpandFunc(xMin, xMax, yMin, yMax, edgeFilter);
		final BiFunction<IntVec2D, IntVec2D, Double> cost = SearchTest::cost;
		final Function<IntVec2D, Double> heuristic = makeHeuristicFunc(goalCoord);
		final List<IntVec2D> result = AStar.aStarSearch(startCoord, isEndgameCheck, expand, cost, heuristic);
		assertTrue(result.size() > 1);
		assertEquals(
		    String.format(
				"startCoord: (%d, %d), result.get(0): (%d, %d)",
				startCoord.x, startCoord.y, result.get(0).x, result.get(0).y),
			startCoord, result.get(0));
		assertEquals(goalCoord, result.get(result.size()-1));
		assertTrue(pathIsConnected(result, expand));
	}

}
