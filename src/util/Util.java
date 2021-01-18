package util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import battlecode.common.*;

/**
 * Contains utility functions/classes.
 */
public class Util {	

	
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
	
	public static <T> T findLeastCostLinear(Iterator<T> iterator, Function<T, Double> costFunc) {
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
	
	public static Direction directionToGoal(MapLocation startCoord, MapLocation goalCoord) {
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
	
	public static <T> Stream<T> streamifyIterator(Iterator<T> iterator) {
		Stream.Builder<T> builder = Stream.builder();
		while (iterator.hasNext()) {
			builder.accept(iterator.next());
		}
		return builder.build();
	}
	
	public static <T> Set<T> legalSetCollect(Stream<T> stream) {
		Iterator<T> iterator = stream.iterator();
		Set<T> resultSet = new HashSet<>();
		while (iterator.hasNext()) {
			resultSet.add(iterator.next());
		}
		return resultSet;
	}
	
	public static void battlecodeAssert(boolean resignIfFalse, String message, RobotController rc) {
		if (!resignIfFalse) {
			System.out.println("ASSESRTION FAIL: " + message);
			rc.resign();
		}
	}

	public static <T> Set<T> removeMatching(Iterable<T> iterable, Predicate<T> predicate) {
		Set<T> removedSet = new HashSet<>();
		Iterator<T> iterator = iterable.iterator();
		while (iterator.hasNext()) {
			// Note: must call next() before remove()
			T element = iterator.next();
			if (predicate.test(element)) {
				removedSet.add(element);
				iterator.remove();
			}
		}
		return removedSet;
	}
	
	@FunctionalInterface
	public static interface GameActionFunction<T, R> {
		public R apply(T t) throws GameActionException;
	}
}
