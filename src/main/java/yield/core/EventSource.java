package yield.core;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import yield.input.FeedingPrevented;
import yield.input.ListenerExceutionFailed;

public abstract class EventSource<Event> extends BaseControlQueueProvider
		implements Yielder<Event> {
	private static final Logger feedlogger = LogManager.getLogger("yield.feed");

	private final Set<EventListener<Event>> listeners = new HashSet<>();

	/**
	 * Publish a new event.
	 */
	protected void feedBoundQueues(Event logEvent) {
		for (EventListener<Event> l : listeners) {
			try {
				l.feed(logEvent);
			} catch (ClassCastException e) {
				feedlogger
						.warn("Failed to feed an event due to incompatible types.",
								e);
				l.getControlQueue().feed(
						new FeedingPrevented(new IllegalArgumentException(
								"Due to incompatible types cannot feed to listener "
										+ l, e)));
			} catch (Exception e) {
				feedlogger.warn("Failed to feed an event.", e);
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
