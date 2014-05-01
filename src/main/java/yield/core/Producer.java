package yield.core;

/**
 * Factory for objects.
 * 
 * @param <T>
 *            Type of created objects.
 */
public abstract class Producer<T> {
	public abstract T make();
}
