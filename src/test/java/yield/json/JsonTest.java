package yield.json;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.output.file.FileAppender;
import yield.test.Collector;

public class JsonTest {

	@Test
	public void testSimpleJoin() throws IOException {
		List<String> inputs = new ArrayList<>();
		inputs.add("2014-03-07T12:23:58.123 [module] INFO test message");
		inputs.add("2014-03-07T12:23:58.123 [dummy] WARNING a warning");
		MappedQueue<String, JsonEvent> input = new MappedQueue<>(
				new ValueMapper<String, JsonEvent>() {

					@Override
					public JsonEvent map(String value) {
						return new JsonEvent(value);
					}
				});
		MappedQueue<JsonEvent, JsonEvent> parser = new MappedQueue<>(
				new JsonGrok("message",
						"^(?<time>[^ ]+) \\[(?<module>[^\\]]+)\\] (?<level>\\w+) (?<message>.+)$"));
		input.getQueue().bind(parser);

		Collector<JsonEvent> occurs = new Collector<>();
		parser.getQueue().bind(occurs);
		parser.getQueue().bind(
				new FileAppender<JsonEvent>(Paths.get("/tmp/jsonout")));

		for (String line : inputs) {
			input.feed(line);
		}

		Assert.assertEquals("module", occurs.get(0).get("module"));
		Assert.assertEquals("dummy", occurs.get(1).get("module"));
	}

	@Test
	public void testTemplating() {
		JsonEvent input = new JsonEvent();
		input.put("test", "abc");
		String result = new Template("test::\" status\"").apply(input);
		Assert.assertEquals("abc status", result);
	}
}