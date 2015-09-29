package yield.config;

import javax.annotation.Nonnull;

import yield.core.EventType;
import yield.core.Yielder;

/**
 * Combine type information and {@link Yielder}.
 */
public class TypedYielder {
	/**
	 * Type of yielded event. Stored as {@link EventType} so it can survive
	 * Java's type erasure.
	 */
	@Nonnull
	public final EventType type;

	@SuppressWarnings("rawtypes")
	@Nonnull
	public final Yielder yielder;

	public TypedYielder(@Nonnull EventType type, Yielder<?> yielder) {
		this.type = type;
		if (yielder == null) {
			throw new IllegalArgumentException("Yielder wasn't provided.");
		}
		this.yielder = yielder;
	}

	@Override
	public String toString() {
		return ":" + type;
	}

	@Nonnull
	public static TypedYielder wrap(@Nonnull EventType type, Yielder<?> yielder) {
		return new TypedYielder(type, yielder);
	}

	/**
	 * Returns a {@link Yielder} after verifying it matches the required type.
	 * 
	 * @param requiredType
	 *            Required event type of {@link Yielder}.
	 * @return Matching {@link Yielder}.
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	public <RequiredType> Yielder<RequiredType> getTypesafe(
			EventType requiredType) {
		if (this.type.isUsableAs(requiredType)) {
			return this.yielder;
		} else {
			throw new RuntimeException("Requires " + requiredType
					+ " but yielder is of type " + this.type);
		}
	}
}
