package yield.input.network;

import yield.core.event.FailureEvent;
import yield.input.ControlEvent;

/**
 * Network connection issue.
 */
public class ConnectionFailed extends FailureEvent<String> implements
		ControlEvent {
	public ConnectionFailed(String message, Exception e) {
		super(new RuntimeException(message, e));
	}
}
