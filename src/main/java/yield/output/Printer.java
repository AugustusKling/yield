package yield.output;

import yield.core.EventListener;
import yield.core.Main;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Prints to standard output stream.
 */
public class Printer<Event> implements EventListener<Event> {

	private String prefix;

	public Printer(String prefix) {
		this.prefix = prefix;
	}

	public Printer(Main main, ObjectNode config) {
		prefix = config.get("prefix").textValue();
	}

	@Override
	public void feed(Event e) {
		System.out.println(prefix + e);
	}

}
