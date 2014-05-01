package yield.config;

import yield.core.Yielder;

/**
 * Combine type information and {@link Yielder}.
 */
public class TypedYielder {
	/**
	 * Type of yielded event. Stored as {@link String} so it can survive Java's
	 * type erasure.
	 */
	public final String type;

	public final Yielder<Object> yielder;

	public TypedYielder(String type, Yielder<Object> yielder) {
		this.type = type;
		this.yielder = yielder;
	}

	@Override
	public String toString() {
		return ":" + type;
	}
}
