package player.util.search;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BFSGenerator<T> {
	private Queue<T> queue;
	private Set<T> closed;
	private Function<T, Set<T>> expandFunc;
	public BFSGenerator(T initialElement, Function<T, Set<T>> expandFunc) {
		queue = new ArrayDeque<>(Arrays.asList(initialElement));  // TODO(theimer): use LinkedList to save bytecode?\
		closed = new HashSet<>(Arrays.asList(initialElement));
		this.expandFunc = expandFunc;
	}
	public T next() {
		T popped = queue.poll();
		if (popped == null) {
			return null;
		}
		Set<T> expandedSet = expandFunc.apply(popped);
		Set<T> filteredSet = expandedSet.stream()
				.filter(elt -> !closed.contains(elt))
				.collect(Collectors.toSet());
		queue.addAll(filteredSet);
		return popped;
	}
}
