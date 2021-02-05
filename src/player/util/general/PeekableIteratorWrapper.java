package player.util.general;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class PeekableIteratorWrapper<T> implements Iterator<T> {

	private final Iterator<T> iterator;  // the wrapped iterator
	private Optional<T> peekedOptional;  // stores an element after it is peeked

	/**
	 * Wraps an iterator and adds peek() functionality.
	 * @param iterator the iterator to wrap.
	 */
	public PeekableIteratorWrapper(final Iterator<T> iterator) {
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
			final T nextElt = this.peekedOptional.get();
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