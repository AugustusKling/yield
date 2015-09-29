package yield.core;

import javax.annotation.Nonnull;

/**
 * Aggregates a series of events and publishes result to a queue.
 * 
 * @param <T>
 *            Type of Events and aggregation result.
 */
public abstract class Aggregator<T, Out> implements SourceProvider<Out> {

	protected final EventQueue<Out> queue;

	public Aggregator(@Nonnull EventType outType) {
		queue = new EventQueue<>(outType);
	}

	/**
	 * Calculates aggregate and pushes result to queue.
	 * 
	 * @param events
	 *            Data for aggregation.
	 */
	protected abstract void aggregate(Iterable<T> events);

	@Override
	public EventQueue<Out> getQueue() {
		return queue;
	}
}
