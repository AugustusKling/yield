package yield.core.windows;

import java.util.ArrayList;
import java.util.List;

import yield.core.Producer;
import yield.core.Window;

/**
 * Retains all events ever encountered. Be aware that this happens in memory and
 * thus should not be used for infinitely running processes.
 */
public class All<T> extends Producer<Window<T>> {
	static class AllWindow<T> extends Window<T> {
		private final List<T> events = new ArrayList<>();

		@Override
		public void feed(T e) {
			events.add(e);

			refreshAggregates();
		}

		@Override
		protected Iterable<T> getEvents() {
			return events;
		}

	}

	@Override
	public Window<T> make() {
		return new AllWindow<>();
	}

}
