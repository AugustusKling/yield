package yield.core.event;

/**
 * Transports a wrapped event.
 * 
 * @param <Event>
 *            Type of event.
 */
public class SuccessEvent<Event> implements MetaEvent<Event> {

	private Event event;

	public SuccessEvent(Event event) {
		this.event = event;
	}

	@Override
	public Event get() {
		return this.event;
	}

}
