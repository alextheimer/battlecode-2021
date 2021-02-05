package player.util.search;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO(theimer): this file is probably dead

public class BfsGenerator<T> {
	private Queue<T> openQueue;  // yet to be expanded elements
	private Set<T> closedSet;  // elements that have been added to openQueue
	private Function<T, Collection<T>> expandFunc;  // function used to expand elements
	
	/***
	 * Generates elements as they would be visited in a BFS search.
	 * 
	 * @param initialElement the root of the BFS search.
	 * @param expandFunc the function used to expand elements of the BFS search.
	 */
	public BfsGenerator(T initialElement, Function<T, Collection<T>> expandFunc) {
		openQueue = new ArrayDeque<>(Arrays.asList(initialElement));
		closedSet = new HashSet<>(Arrays.asList(initialElement));
		this.expandFunc = expandFunc;
	}
	
	public boolean hasNext() {
		return this.openQueue.size() > 0;
	}
	
	/**
	 * Returns the next element (if it exists) of the BfsGenerator.
	 * Note: throws NoSuchElementException if the BfsGenerator is empty.
	 */
	public T next() {
		T popped = openQueue.remove();
		Collection<T> expandedCollection = expandFunc.apply(popped);
		List<T> unclosedList = expandedCollection.stream()
				.filter(elt -> !closedSet.contains(elt))
				.collect(Collectors.toList());
		openQueue.addAll(unclosedList);
		return popped;
	}
}
