package yield.core.partition;

import javax.annotation.Nonnull;

import yield.core.EventQueue;
import yield.core.EventType;
import yield.core.QueueMapper;

public class MapWithKey<Key, Value> extends
		QueueMapper<Value, PartitionedEntry<Key, Value>> {
	private final Key key;

	@Nonnull
	private EventQueue<PartitionedEntry<Key, Value>> queue;

	public MapWithKey(Key key, @Nonnull EventType keyType,
			@Nonnull EventType valueType) {
		this.key = key;
		this.queue = new EventQueue<>(new EventType(PartitionedEntry.class)
				.withGeneric(keyType).withGeneric(valueType));
	}

	@Override
	protected PartitionedEntry<Key, Value> map(Value e) {
		return new PartitionedEntry<Key, Value>(this.key, e);
	}

	@Override
	@Nonnull
	public EventQueue<PartitionedEntry<Key, Value>> getQueue() {
		return this.queue;
	}
};