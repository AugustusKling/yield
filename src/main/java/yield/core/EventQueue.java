package yield.core;

import javax.annotation.Nonnull;

/**
 * Broadcasts incoming events to all listeners.
 */
public class EventQueue<Event> extends EventSource<Event> implements
		EventListener<Event> {
	@Nonnull
	private EventType type;

	public EventQueue(@Nonnull EventType type) {
		this.type = type;
	}

	public EventQueue(@Nonnull Class<Event> class1) {
		this.type = new EventType(class1);
	}

	/**
	 * Publish a new event into the queue.
	 */
	@Override
	public void feed(Event logEvent) {
		super.feedBoundQueues(logEvent);
	}

	@Override
	@Nonnull
	public EventType getOutputType() {
		return type;
	}

	@Override
	@Nonnull
	public EventType getInputType() {
		return type;
	}

}
