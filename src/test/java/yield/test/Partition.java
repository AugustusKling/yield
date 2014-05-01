package yield.test;

import org.junit.Assert;
import org.junit.Test;

import yield.core.Aggregator;
import yield.core.EventQueue;
import yield.core.Producer;
import yield.core.Query;
import yield.core.ValueMapper;
import yield.core.Yielder;
import yield.core.partition.PartitionedEntry;
import yield.core.windows.All;

public class Partition {
	class Event {
		String group;
		int value;

		public Event(String group, int value) {
			this.group = group;
			this.value = value;
		}

		@Override
		public String toString() {
			return group + "=" + value;
		}

		@Override
		public int hashCode() {
			return group.hashCode() + value;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Event) {
				return group.equals(((Event) obj).group)
						&& value == ((Event) obj).value;
			} else {
				return false;
			}
		}
	}

	@Test
	public void test() {
		EventQueue<Event> queue = new EventQueue<>();
		Producer<Aggregator<Event, Event>> sum = new Producer<Aggregator<Event, Event>>() {
			class SumValue extends Aggregator<Event, Event> {

				@Override
				protected void aggregate(Iterable<Event> events) {
					int current = 0;
					for (Event e : events) {
						current = current + e.value;
					}
					Event result = new Event(events.iterator().next().group,
							current);
					queue.feed(result);
				}

			}

			@Override
			public Aggregator<Event, Event> make() {
				return new SumValue();
			}
		};
		Yielder<PartitionedEntry<String, Event>> results = new Query<>(queue)
				.partition(new All<Event>(), sum,
						new ValueMapper<Event, String>() {

							@Override
							public String map(Event value) {
								return value.group;
							}
						}).getQueue();

		Collector<PartitionedEntry<String, Event>> occurs = new Collector<PartitionedEntry<String, Event>>();
		results.bind(occurs);

		queue.feed(new Event("a", 2));
		queue.feed(new Event("b", 3));
		queue.feed(new Event("a", 4));

		Collector<PartitionedEntry<String, Event>> expects = new Collector<PartitionedEntry<String, Event>>();
		expects.feed(new PartitionedEntry<String, Partition.Event>("a",
				new Event("a", 2)));
		expects.feed(new PartitionedEntry<String, Partition.Event>("b",
				new Event("b", 3)));
		expects.feed(new PartitionedEntry<String, Partition.Event>("a",
				new Event("a", 6)));

		Assert.assertEquals(expects, occurs);
	}

}
