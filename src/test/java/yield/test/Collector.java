package yield.test;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import yield.core.BaseControlQueueProvider;
import yield.core.EventListener;
import yield.core.EventType;

public class Collector<Event> extends BaseControlQueueProvider implements
		EventListener<Event> {
	private final List<Event> all = new ArrayList<>();

	@Override
	public void feed(Event e) {
		all.add(e);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Collector) {
			return all.equals(((Collector<?>) obj).all);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return all.hashCode();
	}

	@Override
	public String toString() {
		return all.toString();
	}

	public Event get(int index) {
		return all.get(index);
	}

	public Event getLast() {
		return all.get(all.size() - 1);
	}

	@Override
	@Nonnull
	public EventType getInputType() {
		return EventType.ALL;
	}
}
