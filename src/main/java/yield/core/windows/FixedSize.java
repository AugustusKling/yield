package yield.core.windows;

import java.util.ArrayList;
import java.util.List;

import yield.core.Window;

/**
 * Retains the most recent events up to the given window length.
 */
public class FixedSize<Event> extends Window<Event> {
	private final int size;

	private final List<Event> events;

	public FixedSize(int size) {
		this.size = size;
		events = new ArrayList<>(size);
	}

	@Override
	public void feed(Event e) {
		if (events.size() == this.size) {
			events.remove(0);
		}
		events.add(e);

		refreshAggregates();
	}

	@Override
	protected Iterable<Event> getEvents() {
		return events;
	}

}
