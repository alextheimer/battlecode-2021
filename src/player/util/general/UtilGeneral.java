package player.util.general;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import battlecode.common.*;

import java.util.Queue;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Contains utility functions/classes.
 */
public class UtilGeneral {
	
	public static String stringifyStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();	
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
	
	@Deprecated
	public static <T> Set<T> legalSetCollect(Stream<T> stream) {
		Iterator<T> iterator = stream.iterator();
		Set<T> resultSet = new HashSet<>();
		while (iterator.hasNext()) {
			resultSet.add(iterator.next());
		}
		return resultSet;
	}
	
	public static <C extends Collection<T>, T> void legalCollect(Stream<T> stream, C collection) {
		Iterator<T> iterator = stream.iterator();
		while (iterator.hasNext()) {
			collection.add(iterator.next());
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
}
