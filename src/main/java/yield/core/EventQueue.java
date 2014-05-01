package yield.core;

/**
 * Broadcasts incoming events to all listeners.
 */
public class EventQueue<Event> extends EventSource<Event> implements
		EventListener<Event> {

	/**
	 * Publish a new event into the queue.
	 */
	@Override
	public void feed(Event logEvent) {
		super.feedBoundQueues(logEvent);
	}

}
