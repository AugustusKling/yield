package yield.input;

import yield.core.EventQueue;

/**
 * Means of control for error handling and other exceptional circumstances.
 */
public class ControlQueue extends EventQueue<ControlEvent> {
	public ControlQueue() {
		super(ControlEvent.class);
	}

	@Override
	public void feed(ControlEvent logEvent) {
		super.feed(logEvent);
	}
}
