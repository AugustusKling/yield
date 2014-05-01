package yield.core;

/**
 * Any object that might emit an event.
 */
public interface Yielder<OutEvent> {
	/**
	 * Register a new listener that get posted about every event emitted.
	 * 
	 * @param listener
	 *             Event listener
	 */
	public void bind(EventListener<OutEvent> listener);
}
