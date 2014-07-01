package yield.config.function;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import yield.config.ConfigReader;
import yield.config.TypedYielder;
import yield.core.EventQueue;
import yield.core.Yielder;
import yield.json.JsonEvent;
import yield.test.Collector;

public class WhereTest {

	@Test
	public void positiveEquals() throws Exception {
		Where where = new Where();

		EventQueue<JsonEvent> inputQueue = new EventQueue<>();
		TypedYielder input = TypedYielder.wrap(JsonEvent.class.getName(),
				inputQueue);
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, input);

		TypedYielder positiveEqualsFilter = where.getSource("x=\"match test\"",
				context);

		Collector<JsonEvent> remaining = new Collector<>();
		Yielder<JsonEvent> filtered = positiveEqualsFilter
				.getTypesafe(JsonEvent.class.getName());
		filtered.bind(remaining);

		inputQueue.feed(new JsonEvent("{\"x\":\"test\"}"));
		inputQueue.feed(new JsonEvent("{\"x2\":\"match test\"}"));
		JsonEvent logEvent = new JsonEvent("{\"x\":\"match test\"}");
		inputQueue.feed(logEvent);

		Collector<JsonEvent> expected = new Collector<>();
		expected.feed(logEvent);
		Assert.assertEquals(expected, remaining);
	}

	@Test
	public void negativeEquals() throws Exception {
		Where where = new Where();

		EventQueue<JsonEvent> inputQueue = new EventQueue<>();
		TypedYielder input = TypedYielder.wrap(JsonEvent.class.getName(),
				inputQueue);
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, input);

		TypedYielder positiveEqualsFilter = where.getSource(
				"not x=\"match test\"", context);

		Collector<JsonEvent> remaining = new Collector<>();
		Yielder<JsonEvent> filtered = positiveEqualsFilter
				.getTypesafe(JsonEvent.class.getName());
		filtered.bind(remaining);

		JsonEvent logEvent1 = new JsonEvent("{\"x\":\"test\"}");
		inputQueue.feed(logEvent1);
		JsonEvent logEvent2 = new JsonEvent("{\"x2\":\"match test\"}");
		inputQueue.feed(logEvent2);
		JsonEvent logEvent3 = new JsonEvent("{\"x\":\"match test\"}");
		inputQueue.feed(logEvent3);

		Collector<JsonEvent> expected = new Collector<>();
		expected.feed(logEvent1);
		expected.feed(logEvent2);
		Assert.assertEquals(expected, remaining);
	}
}
