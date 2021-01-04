package main;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

// TODO(theimer): make this exclusive to A*
//     (also probably make this file AStar.java if no other searches are needed)
class Node<T> {
	private final T element;
	private double costTo;  // the best-known cost to this Node from some initial Node.
	private double costSum;  // the best-known sum of costTo and some heuristic
	private Node<T> parent;  // the parent that gives costTo and costSum

	public Node(final T element, final double costTo, final double costSum, final Node<T> parent) {
		this.element = element;
		this.costTo = costTo;
		this.costSum = costSum;
		this.parent = parent;
	}

	/**
	 * Updates the Node's best-known parent (and all other relevant info).
	 */
	public void updateParent(final Node<T> parent, final double costTo, final double costSum) {
		// Note: this method prevents mistakes where <3 of these attributes are updated.
		this.parent = parent;
		this.costTo = costTo;
		this.costSum = costSum;
	}

	public double getCostTo() {
		return this.costTo;
	}

	public double getCostSum() {
		return this.costSum;
	}

	public Node<T> getParent() {
		return this.parent;
	}

	public T getElement() {
		// Don't necessarily need this method, but it's here for the sake of
		// uniformity with the other getters.
		return this.element;
	}
}

/**
 * Implements various search algorithms.
 */
public class Search {

	/**
	 * Returns the path of Node elements to the tail element.
	 */
	private static <T> List<T> makeElementPath(final Node<T> tail) {
		final Stack<T> path = new Stack<>();
		Node<T> ptr = tail;
		while (ptr != null) {
			path.push(ptr.getElement());
			ptr = ptr.getParent();
		}
		return Collections.unmodifiableList(path);
	}

	private static <T> List<T> makeNullPath() {
		return List.of();
	}

	// TODO(theimer): mention immutability of return type
	/**
	 * Returns a shortest path between two T.
	 * @param <T> TODO(theimer)
	 * @param startObj begin the search here. Must lie within valid search space.
	 * @param isEndgameCheck returns true if and only if its argument lies in the endgame.
	 * @param expand returns the set of all T adjacent to its argument.
	 * @param cost returns the cost between its two T arguments. Must return a value >= 0.
	 * @param heuristic returns an estimated cost to the endgame from the argument T. Must be
	 *     both admissible and consistent.
	 * @return a List of T such that that the first element is startObj, and the final T makes
	 *     isEndgameCheck return true. Each sequential element was generated by expand() on the
	 *     element before it (i.e. they are all connected by edges on the implicit graph described
	 *     by expand()).
	 *
	 *     Returns a List of length 1 [startObj] if startObj lies within the endgame.
	 *     Returns an empty List if there is no path from startObj to the endgame.
	 */
	public static <T> List<T> aStar(final T startObj, final Predicate<T> isEndgameCheck, final Function<T, Set<T>> expand,
			final BiFunction<T, T, Double> cost, final Function<T, Double> heuristic) {
		// build the start node
		final Node<T> startNode;
		{
			// these variables only exist as clarification
			final double startCost = 0;
			final double startHeuristic = heuristic.apply(startObj);
			final Node<T> startParent = null;
			startNode = new Node<>(startObj, startCost, startHeuristic, startParent);
		}

		// initialize the data structures
		final PriorityQueue<Node<T>> pQueue = new PriorityQueue<>(List.of(startNode));
		final Map<T, Node<T>> nodeMap = new HashMap<>(Map.of(startObj, startNode));  // contains only nodes in pQueue
		final Set<T> closed = new HashSet<>();  // been popped from pQueue.

		// begin A* algorithm
		while (pQueue.size() > 0) {
			final Node<T> popped = pQueue.poll();
			nodeMap.remove(popped.getElement());
			closed.add(popped.getElement());
			assert popped != null : "null Node popped from the queue!";
			if (isEndgameCheck.test(popped.getElement())) {
				// found an element in the endgame; ready to return a path.
				return Search.makeElementPath(popped);
			}
			final Set<T> expanded = expand.apply(popped.getElement());
			for (final T expandedObj : expanded) {
				if (closed.contains(expandedObj)) {
					// don't want to add it to pQueue or update its costs
					continue;
				}
				final double expandedCost = popped.getCostTo() + cost.apply(popped.getElement(), expandedObj);
				final double expandedHeuristic = heuristic.apply(expandedObj);
				final double expandedSum = expandedCost + expandedHeuristic;
				if (nodeMap.containsKey(expandedObj)) {
					// pQueue contains expandedObj's Node; check if we need to update its costs
					// (i.e. we've arrived from a parent along a more-optimal path)
					final Node<T> containedNode = nodeMap.get(expandedObj);
					if (containedNode.getCostSum() > expandedSum) {
						// found a better path; update.
						// TODO(theimer): make this faster if times out!
						pQueue.remove(containedNode);
						containedNode.updateParent(popped, expandedCost, expandedSum);
						pQueue.add(containedNode);
					}
				}
				else {  // neither open nor closed contains expandedObj
					// we haven't seen this expandedObj yet; add it to pQueue/nodeMap
					final Node<T> expandedNode = new Node<>(expandedObj, expandedCost, expandedSum, popped);
					pQueue.add(expandedNode);
					nodeMap.put(expandedObj, expandedNode);
				}
			}
		}
		// reach here only if no path was found; return the "null" path.
		return Search.makeNullPath();
	}
}