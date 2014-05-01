package yield.core;

import yield.core.partition.PartitionedEntry;

/**
 * Shorthands.
 */
public class Query<T> implements SourceProvider<T> {
	private final Yielder<T> queue;

	public Query(Yielder<T> queue) {
		this.queue = queue;
	}

	public Query(SourceProvider<T> p) {
		this.queue = p.getQueue();
	}

	public Query<T> filter(Filter<T> filter) {
		queue.bind(filter);

		return new Query<>(filter);
	}

	public <Out> Query<Out> within(Producer<Window<T>> windowProducer,
			Producer<Aggregator<T, Out>> aggregatorProducer) {
		Window<T> window = windowProducer.make();
		Aggregator<T, Out> aggregator = aggregatorProducer.make();

		queue.bind(window);
		window.bind(aggregator);

		return new Query<>(aggregator);
	}

	public <O, Out> Query<Out> join(Yielder<O> other,
			final Joiner<T, O, Out> joiner) {
		Join<T, O, Out> join = new Join<T, O, Out>(this.getQueue(), other) {

			@Override
			public Out join(T lastValue, O lastValue2) {
				return joiner.join(lastValue, lastValue2);
			}
		};
		return new Query<>(join.getQueue());
	}

	public <Out> Query<Out> map(QueueMapper<T, Out> mapper) {
		queue.bind(mapper);
		return new Query<>(mapper.getQueue());
	}

	public <Key, Out> Query<PartitionedEntry<Key, Out>> partition(
			Producer<Window<T>> windowProducer,
			Producer<Aggregator<T, Out>> aggregatorProducer,
			ValueMapper<T, Key> grouper) {
		Partitioner<T, Key, Out> partitioner = new Partitioner<>(
				windowProducer, aggregatorProducer, grouper);
		queue.bind(partitioner);
		return new Query<>(partitioner);
	}

	@Override
	public Yielder<T> getQueue() {
		return queue;
	}
}
