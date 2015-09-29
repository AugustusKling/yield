package yield.config.function;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import yield.config.ConfigReader;
import yield.config.TypedYielder;
import yield.core.EventQueue;
import yield.core.EventType;
import yield.core.Yielder;
import yield.json.JsonEvent;
import yield.test.Collector;

public class MutateTest {
	@Test
	public void parse() {
		EventQueue<JsonEvent> inputQueue = new EventQueue<>(JsonEvent.class);
		TypedYielder input = TypedYielder.wrap(new EventType(JsonEvent.class),
				inputQueue);
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, input);

		Collector<JsonEvent> results = new Collector<>();
		TypedYielder m = new Mutate().getSource(
				"+greeting \"Hoi, \" :: name :: \"!\"", context);
		Yielder<JsonEvent> y = m.getTypesafe(new EventType(JsonEvent.class));
		y.bind(results);

		inputQueue.feed(new JsonEvent("{\"name\":\"Anna\"}"));
		inputQueue.feed(new JsonEvent("{\"name\":\"Bänz\"}"));
		inputQueue.feed(new JsonEvent());

		Collector<JsonEvent> expected = new Collector<>();
		expected.feed(new JsonEvent(
				"{\"name\":\"Anna\", \"greeting\":\"Hoi, Anna!\"}"));
		expected.feed(new JsonEvent(
				"{\"name\":\"Bänz\", \"greeting\":\"Hoi, Bänz!\"}"));
		expected.feed(new JsonEvent());
		Assert.assertEquals(expected, results);
	}
}
