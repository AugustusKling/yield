package yield.input;

import yield.core.event.FailureEvent;

/**
 * Throw if an event could not be provided to a listener.
 * 
 * @see ListenerExceutionFailed Failures to process the events by listeners are
 *      handled by {@link ListenerExceutionFailed}.
 */
public class FeedingPrevented extends FailureEvent<Object> implements
		ControlEvent {

	public FeedingPrevented(Exception e) {
		super(e);
	}

}
