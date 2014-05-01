package yield.core;

import java.util.HashSet;
import java.util.Set;

public abstract class EventSource<Event> implements Yielder<Event> {

	private final Set<EventListener<Event>> listeners = new HashSet<>();

	/**
	 * Publish a new event.
	 */
	protected void feedBoundQueues(Event logEvent) {
		for (EventListener<Event> l : listeners) {
			try {
				l.feed(logEvent);
			} catch (ClassCastException e) {
				System.err
						.println("Due to incompatible types cannot feed to listener "
								+ l);
			}
		}
	}

	@Override
	public void bind(EventListener<Event> listener) {
		listeners.add(listener);
	}
}
