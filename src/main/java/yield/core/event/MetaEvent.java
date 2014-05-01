package yield.core.event;

/**
 * Wrapper that can either transport an event or an error.
 * 
 * @param <Event>
 *            Type of event payload.
 */
public interface MetaEvent<Event> {
	/**
	 * @return Wrapped event.
	 * @throws Exception
	 *             Transported error.
	 */
	public Event get() throws Exception;
}
