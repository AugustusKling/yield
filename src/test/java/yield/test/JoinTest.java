package yield.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import yield.core.EventQueue;
import yield.core.EventType;
import yield.core.Join;
import yield.core.Joiner;
import yield.core.Query;

public class JoinTest {

	@Test
	public void testSimpleJoin() {
		EventQueue<String> queueA = new EventQueue<>(String.class);
		EventQueue<String> queueB = new EventQueue<>(String.class);
		EventQueue<List<String>> joined = new Join<String, String, List<String>>(
				queueA, queueB, new EventType(String.class)) {

			@Override
			public List<String> join(String lastValue, String lastValue2) {
				return JoinTest.this.join(lastValue, lastValue2);
			}

		}.getQueue();

		Collector<List<String>> occurs = new Collector<>();
		joined.bind(occurs);

		queueA.feed("a1");
		queueA.feed("a2");
		queueB.feed("b1");
		queueB.feed("b2");
		queueA.feed("a3");

		Collector<List<String>> expects = new Collector<>();
		expects.feed(join("a2", "b1"));
		expects.feed(join("a2", "b2"));
		expects.feed(join("a3", "b2"));

		Assert.assertEquals(expects, occurs);
	}

	@Test
	public void testQuery() {
		EventQueue<String> queueA = new EventQueue<>(String.class);
		EventQueue<String> queueB = new EventQueue<>(String.class);

		Query<List<String>> query = new Query<>(queueA).join(queueB,
				new Joiner<String, String, List<String>>() {

					@Override
					public List<String> join(String lastValue, String lastValue2) {
						return JoinTest.this.join(lastValue, lastValue2);
					}

				}, new EventType(List.class).withGeneric(String.class));

		Collector<List<String>> occurs = new Collector<>();
		query.getQueue().bind(occurs);

		queueA.feed("a1");
		queueA.feed("a2");
		queueB.feed("b1");
		queueB.feed("b2");
		queueA.feed("a3");

		Collector<List<String>> expects = new Collector<>();
		expects.feed(join("a2", "b1"));
		expects.feed(join("a2", "b2"));
		expects.feed(join("a3", "b2"));

		Assert.assertEquals(expects, occurs);
	}

	List<String> join(String lastValue, String lastValue2) {
		List<String> joined = new ArrayList<>();
		joined.add(lastValue);
		joined.add(lastValue2);
		return joined;
	}
}
