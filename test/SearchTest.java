package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import javafx.util.Pair;

class IntPair extends Pair<Integer, Integer> {public IntPair(int i1, int i2) {super(i1, i2);}}

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
	 */
	
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
