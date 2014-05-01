package yield.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import yield.core.EventQueue;
import yield.core.pattern.Transition;

public class Pattern {

	@Test
	public void test() {
		yield.core.pattern.Pattern<String> firstAAnythingB = new yield.core.pattern.Pattern<>(
				"start");
		firstAAnythingB.addTransition(new Transition<String>("start", "a") {

			@Override
			public boolean matches(String e) {
				return e.equals("A");
			}
		});
		firstAAnythingB.addTransition(new Transition<String>("a", "b") {

			@Override
			public boolean matches(String e) {
				return e.equals("B");
			}
		});

		EventQueue<String> q = new EventQueue<String>();
		q.bind(firstAAnythingB);

		Collector<String> results = new Collector<>();
		firstAAnythingB.getQueue().bind(results);

		// Does not change output.
		q.feed("hallo");

		Collector<String> expected = new Collector<>();
		assertEquals(expected, results);

		// Triggers: start → a
		q.feed("A");
		expected.feed("a");
		assertEquals(expected, results);

		// Does not change output.
		q.feed("other");
		assertEquals(expected, results);

		// Triggers: a → b
		q.feed("B");
		expected.feed("b");
		assertEquals(expected, results);

		// Does not change output.
		q.feed("other");
		assertEquals(expected, results);
	}

}
