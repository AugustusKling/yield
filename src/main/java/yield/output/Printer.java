package yield.output;

import javax.annotation.Nonnull;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.core.EventType;

/**
 * Prints to standard output stream.
 */
public class Printer<Event> extends BaseControlQueueProvider implements
		EventListener<Event> {

	private String prefix;

	public Printer(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void feed(Event e) {
		System.out.println(prefix + e);
	}

	@Override
	@Nonnull
	public EventType getInputType() {
		return EventType.ALL;
	}
}
