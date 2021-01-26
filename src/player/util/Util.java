package player.util;

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

import java.util.Queue;
import java.util.ArrayDeque;

/**
 * Contains utility functions/classes.
 */
public class Util {
	
	public static class PeekableIteratorWrapper<T> implements Iterator<T> {
		
		// TODO(theimer): move to different file?
		
		private Iterator<T> iterator;  // the wrapped iterator
		private Optional<T> peekedOptional;  // stores an element after it is peeked
		
		/**
		 * Wraps an iterator and adds peek() functionality.
		 * @param iterator the iterator to wrap.
		 */
		public PeekableIteratorWrapper(Iterator<T> iterator) {
			this.iterator = iterator;
			this.peekedOptional = Optional.empty();
		}
		
		/**
		 * Instance is either in a "peeked" or "unpeeked" state.
		 * 
		 * If peeked, the stored iterator's next element is stored in the Optional field;
		 *     this is returned from all subsequent peek() calls.
		 * If unpeeked, the Optional field is empty.
		 * 
		 *                  peek
		 * /---(unpeeked) -------> (peeked) ----\
		 * |      ^  ^                |  ^      |
		 * \------/  \---------------/   \------/
		 *   next           next           peek
		 */
		
		@Override
		public boolean hasNext() {
			return (this.peekedOptional.isPresent() || this.iterator.hasNext());
		}
		
		@Override
		public T next() {
			if (this.peekedOptional.isPresent()) {
				// peek() was previously called; clear it out before returning its element.
				T nextElt = this.peekedOptional.get();
				this.peekedOptional = Optional.empty();
				return nextElt;
				
			} else {
				// leave `peekedOptional` empty.
				return this.iterator.next();
			}
		}
		
		/**
		 * Returns the element that would be returned on the next next() call.
		 * @throws NoSuchElementException if no additional elements exist.
		 * 
		 */
		public T peek() {
			if (!this.peekedOptional.isPresent()) {
				// peek this element for the first time.
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
}
