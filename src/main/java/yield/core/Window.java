package yield.core;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Event store with retention policy. Events are not persisted forever but kept
 * in a sliding, not necessarily ordered or fixed-size, window.
 */
public abstract class Window<T> extends BaseControlQueueProvider implements
		EventListener<T> {
	private final Set<Aggregator<T, ?>> listeners = new HashSet<>();

	/**
	 * Instructs all bound aggregates to refresh and publish an updated
	 * aggregation.
	 */
	protected void refreshAggregates() {
		Iterable<T> events = getEvents();
		for (Aggregator<T, ?> aggregator : listeners) {
			aggregator.aggregate(events);
		}
	}

	/**
	 * @return Content of the window.
	 */
	protected abstract Iterable<T> getEvents();

	/**
	 * Register another aggregator to be notified whenever the window contents
	 * change.
	 */
	public void bind(Aggregator<T, ?> agg) {
		listeners.add(agg);
	}

	@Override
	@Nonnull
	public EventType getInputType() {
		return EventType.ALL;
	}
}
