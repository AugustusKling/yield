package yield.core;

import javax.annotation.Nonnull;

/**
 * Any object that might emit an event.
 */
public interface Yielder<OutEvent> extends ControlQueueProvider {
	/**
	 * Register a new listener that get posted about every event emitted.
	 * 
	 * @param listener
	 *             Event listener
	 */
	public void bind(EventListener<OutEvent> listener);

	/**
	 * @return Type of event that this yielder emits.
	 */
	@Nonnull
	EventType getOutputType();
}
