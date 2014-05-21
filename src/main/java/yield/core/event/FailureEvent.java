package yield.core.event;

/**
 * Transports an error.
 * 
 * @param <Event>
 *            Type of wrapped event (which is always undefined). Only present to
 *            satisfy the type system.
 */
public class FailureEvent<Event> implements MetaEvent<Event> {
	private Exception e;

	public FailureEvent(Exception e) {
		this.e = e;
	}

	@Override
	public Event get() throws Exception {
		throw e;
	}

	@Override
	public String toString() {
		return e.toString();
	}
}
