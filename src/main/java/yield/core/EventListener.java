package yield.core;

import javax.annotation.Nonnull;

import yield.input.ListenerExceutionFailed;

public interface EventListener<Event> extends ControlQueueProvider {
	/**
	 * Invoked when a new event occurs. Listeners that fail execution should
	 * throw a {@link ListenerExceutionFailed} exception.
	 * 
	 * @param e
	 *            Event data.
	 */
	void feed(Event e);

	/**
	 * @return Type of event that this listener is able to process.
	 */
	@Nonnull
	EventType getInputType();
}
