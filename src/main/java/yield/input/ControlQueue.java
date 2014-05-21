package yield.input;

import yield.core.EventQueue;

/**
 * Means of control for error handling and other exceptional circumstances.
 */
public class ControlQueue extends EventQueue<ControlEvent> {
	@Override
	public void feed(ControlEvent logEvent) {
		super.feed(logEvent);
	}
}
