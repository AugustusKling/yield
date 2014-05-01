package yield.core.partition;

import yield.core.QueueMapper;

public class MapWithKey<Key, Value> extends
		QueueMapper<Value, PartitionedEntry<Key, Value>> {
	private final Key key;

	public MapWithKey(Key key) {
		this.key = key;
	}

	@Override
	protected PartitionedEntry<Key, Value> map(Value e) {
		return new PartitionedEntry<Key, Value>(this.key, e);
	}
};