package yield.core;

import java.util.HashMap;
import java.util.Map;

import yield.core.partition.MapWithKey;
import yield.core.partition.PartitionedEntry;

/**
 * Spread events from one queue to multiple output queues.
 */
public class Partitioner<In, Key, Out> implements EventListener<In>,
		SourceProvider<PartitionedEntry<Key, Out>> {

	private final EventQueue<PartitionedEntry<Key, Out>> resultQueue = new EventQueue<>();

	private final Map<Key, EventQueue<In>> partitions = new HashMap<>();

	private final Producer<Aggregator<In, Out>> aggregator;

	private final Producer<Window<In>> window;

	/**
	 * Strategy to decide into which output queue events are routed.
	 */
	private final ValueMapper<In, Key> grouper;

	public Partitioner(Producer<Window<In>> window,
			Producer<Aggregator<In, Out>> aggregator,
			ValueMapper<In, Key> grouper) {
		this.window = window;
		this.aggregator = aggregator;
		this.grouper = grouper;
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
			partitionQueue = new EventQueue<In>();
			Yielder<Out> aggregatedPartition = new Query<>(partitionQueue)
					.within(window, aggregator).getQueue();
			partitions.put(key, partitionQueue);

			MapWithKey<Key, Out> mapper = new MapWithKey<>(key);
			aggregatedPartition.bind(mapper);
			mapper.getQueue().bind(resultQueue);
		}
		return partitionQueue;
	}
}
