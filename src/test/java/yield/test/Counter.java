package yield.test;

import javax.annotation.Nonnull;

import yield.core.EventQueue;
import yield.core.SourceProvider;

/**
 * Generates events (incrementing counter).
 */
public class Counter<T> implements SourceProvider<T> {

	public static abstract class Producer<T> {

		public abstract T produce(int value);

	}

	protected final EventQueue<T> queue;

	public Counter(@Nonnull Class<T> outType) {
		queue = new EventQueue<>(outType);
	}

	@Override
	public EventQueue<T> getQueue() {
		return queue;
	}

	public void run(int upperBound, Producer<T> producer) {
		for (int i = 0; i < upperBound; i++) {
			T event = producer.produce(i);
			queue.feed(event);
		}
	}

}
