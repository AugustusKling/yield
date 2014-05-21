package yield.core;

import java.util.HashSet;
import java.util.Set;

import yield.input.FeedingPrevented;
import yield.input.ListenerExceutionFailed;

public abstract class EventSource<Event> extends BaseControlQueueProvider
		implements Yielder<Event> {

	private final Set<EventListener<Event>> listeners = new HashSet<>();

	/**
	 * Publish a new event.
	 */
	protected void feedBoundQueues(Event logEvent) {
		for (EventListener<Event> l : listeners) {
			try {
				l.feed(logEvent);
			} catch (ClassCastException e) {
				l.getControlQueue().feed(
						new FeedingPrevented(new IllegalArgumentException(
								"Due to incompatible types cannot feed to listener "
										+ l, e)));
			} catch (Exception e) {
				l.getControlQueue().feed(
						new ListenerExceutionFailed<>(logEvent, e));
			}
		}
	}

	@Override
	public void bind(EventListener<Event> listener) {
		listeners.add(listener);
	}

}
