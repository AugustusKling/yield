package yield.core;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.core.partition.MapWithKey;
import yield.core.partition.PartitionedEntry;

/**
 * Spread events from one queue to multiple output queues.
 */
public class Partitioner<In, Key, Out> extends BaseControlQueueProvider
		implements EventListener<In>,
		SourceProvider<PartitionedEntry<Key, Out>> {

	private final EventQueue<PartitionedEntry<Key, Out>> resultQueue;

	private final Map<Key, EventQueue<In>> partitions = new HashMap<>();

	private final Producer<Aggregator<In, Out>> aggregator;

	private final Producer<Window<In>> window;

	/**
	 * Strategy to decide into which output queue events are routed.
	 */
	private final ValueMapper<In, Key> grouper;

	@Nonnull
	private EventType inType;

	@Nonnull
	private EventType keyType;

	@Nonnull
	private EventType outType;

	public Partitioner(Producer<Window<In>> window,
			Producer<Aggregator<In, Out>> aggregator,
			ValueMapper<In, Key> grouper, @Nonnull EventType inType,
			@Nonnull EventType keyType, @Nonnull EventType outType) {
		this.window = window;
		this.aggregator = aggregator;
		this.grouper = grouper;

		this.inType = inType;
		this.keyType = keyType;
		this.outType = outType;

		this.resultQueue = new EventQueue<>(new EventType(
				PartitionedEntry.class).withGeneric(keyType).withGeneric(
				outType));
	}

	@Override
	public EventQueue<PartitionedEntry<Key, Out>> getQueue() {
		return resultQueue;
	}

	@Override
	public void feed(In e) {
		Key key = grouper.map(e);
		getPartitionQueue(key).feed(e);
	}

	private EventQueue<In> getPartitionQueue(final Key key) {
		EventQueue<In> partitionQueue;
		if (partitions.containsKey(key)) {
			partitionQueue = partitions.get(key);
		} else {
			partitionQueue = new EventQueue<In>(inType);
			Yielder<Out> aggregatedPartition = new Query<>(partitionQueue)
					.within(window, aggregator).getQueue();
			partitions.put(key, partitionQueue);

			MapWithKey<Key, Out> mapper = new MapWithKey<>(key, keyType,
					outType);
			aggregatedPartition.bind(mapper);
			mapper.getQueue().bind(resultQueue);
		}
		return partitionQueue;
	}

	@Override
	@Nonnull
	public EventType getInputType() {
		return inType;
	}
}
