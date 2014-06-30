package yield.config;

import javax.annotation.Nonnull;

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

	@SuppressWarnings("unchecked")
	@Nonnull
	public static TypedYielder wrap(String type,
			Yielder<? extends Object> yielder) {
		return new TypedYielder(type, (Yielder<Object>) yielder);
	}

	/**
	 * Returns a {@link Yielder} after verifying it matches the required type.
	 * 
	 * @param requiredType
	 *            Required event type of {@link Yielder}.
	 * @param yielderName
	 *            Identifier in given {@code context}.
	 * @param context
	 *            All known {@link Yielder}s.
	 * @return Matching {@link Yielder} from {@code context}.
	 */
	@SuppressWarnings("unchecked")
	public <RequiredType> Yielder<RequiredType> getTypesafe(String requiredType) {
		if (this.type.equals(requiredType)) {
			return (Yielder<RequiredType>) this.yielder;
		} else {
			throw new RuntimeException("Requires " + requiredType
					+ " but yielder is of type " + this.type);
		}
	}
}
