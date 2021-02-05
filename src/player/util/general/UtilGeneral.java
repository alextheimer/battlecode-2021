package player.util.general;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Contains utility functions/classes.
 */
public class UtilGeneral {
	
	/**
	 * Returns the throwable's stack trace as a String.
	 */
	public static String stringifyStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();	
	}
	
	/**
	 * Returns the element of least cost.
	 * 
	 * @param iterable must have size > 0
	 * @param costFunc maps each T to a value.
	 * @return the element that returned the smallest value from costFunc.
	 */
	public static <T> T getLeastCostLinear(Iterable<T> iterable, Function<T, Double> costFunc) {
		Iterator<T> iterator = iterable.iterator();
		assert iterator.hasNext();
		T leastCostElt = iterator.next();
		double leastCost = costFunc.apply(leastCostElt);
		for (T elt : iterable) {
			double eltCost = costFunc.apply(elt);
			if (eltCost < leastCost) {
				leastCostElt = elt;
				leastCost = eltCost;
			}
		}
		return leastCostElt;
	}
	
	/**
	 * Collects a stream into an existing Collection instance.
	 * 
	 * Note:
	 *     The Battlecode backend does not support Collectors,
	 *     so this acts as a substitute.
	 */
	public static <C extends Collection<T>, T> void legalCollect(Stream<T> stream, C collection) {
		Iterator<T> iterator = stream.iterator();
		iterator.forEachRemaining(collection::add);
	}
}
