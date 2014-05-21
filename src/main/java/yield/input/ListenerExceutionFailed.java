package yield.input;

import yield.core.event.FailureEvent;

/**
 * Throw if a listener was unable to process an event.
 * 
 * @param <Event>
 *            Type of event that should have been processed.
 */
public class ListenerExceutionFailed<Event> extends FailureEvent<Event>
		implements ControlEvent {
	/**
	 * Event which could not be handled properly by the listener.
	 */
	private Event logEvent;

	public ListenerExceutionFailed(Event logEvent, String message, Exception e) {
		this(logEvent, new Exception(message, e));
	}

	public ListenerExceutionFailed(Event logEvent, String message) {
		this(logEvent, new Exception(message));
	}

	public ListenerExceutionFailed(Event logEvent, Exception e) {
		super(new Exception(e));
		this.logEvent = logEvent;
	}

	public Event getLogEvent() {
		return logEvent;
	}

}
