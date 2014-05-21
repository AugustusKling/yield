package yield.output;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;

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

}
