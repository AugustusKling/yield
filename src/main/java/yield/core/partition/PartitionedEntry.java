package yield.core.partition;

/**
 * Event and derived group identifier (partition key).
 * 
 * @param <Key>
 *            Type of partition key.
 * @param <In>
 *            Type of event.
 */
public class PartitionedEntry<Key, In> {

	private final Key key;
	private final In value;

	public PartitionedEntry(Key key, In value) {
		this.key = key;
		this.value = value;
	}

	public Key getKey() {
		return key;
	}

	public In getValue() {
		return value;
	}

	@Override
	public String toString() {
		return key + "→" + value;
	}

	@Override
	public int hashCode() {
		return key.hashCode() + value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PartitionedEntry) {
			return getKey().equals(((PartitionedEntry<?, ?>) obj).getKey())
					&& getValue().equals(
							((PartitionedEntry<?, ?>) obj).getValue());
		} else {
			return false;
		}
	}
}