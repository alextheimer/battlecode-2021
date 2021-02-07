package player.util.general;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Contains utility functions/classes.
 */
public class UtilGeneral {

	// Battlecode backend instantiates all Randoms with the same seed!
	public static final Random RANDOM = new Random();

	/**
	 * Returns the throwable's stack trace as a String.
	 */
	public static String stringifyStackTrace(final Throwable throwable) {
		final StringWriter stringWriter = new StringWriter();
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
	public static <T> T getLeastCostLinear(final Iterable<T> iterable, final Function<T, Double> costFunc) {
		final Iterator<T> iterator = iterable.iterator();
		assert iterator.hasNext();
		T leastCostElt = iterator.next();
		double leastCost = costFunc.apply(leastCostElt);
		for (final T elt : iterable) {
			final double eltCost = costFunc.apply(elt);
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
	public static <C extends Collection<T>, T> void legalCollect(final Stream<T> stream, final C collection) {
		final Iterator<T> iterator = stream.iterator();
		// forEachRemaining refuses to work; possible conflict with Battlecode backend?
		while (iterator.hasNext()) {
			collection.add(iterator.next());
		}
	}
}
