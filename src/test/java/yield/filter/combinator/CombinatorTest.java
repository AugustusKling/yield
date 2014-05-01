package yield.filter.combinator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Assert;
import org.junit.Test;

import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.input.shipper.ShipperFile;
import yield.json.JsonEvent;
import yield.json.JsonGrok;
import yield.output.file.FileAppender;
import yield.test.Collector;

public class CombinatorTest {
	@Test
	public void testLogFiles() throws IOException {
		RegExCombinator combinator = new RegExCombinator(
				"^\\s+.*|^\\s*$|^Caused by: .*");
		ShipperFile shipper = new ShipperFile();
		shipper.getQueue().bind(combinator);

		MappedQueue<String, JsonEvent> input = new MappedQueue<>(
				new ValueMapper<String, JsonEvent>() {

					@Override
					public JsonEvent map(String value) {
						return new JsonEvent(value);
					}
				});
		combinator.getQueue().bind(input);
		MappedQueue<JsonEvent, JsonEvent> parser = new MappedQueue<>(
				new JsonGrok("message",
						"^(?<time>[^ ]+) (?<level>\\w+)\\s+\\[(?<module>[^\\]]+)\\] (?<message>.+)$"));
		input.getQueue().bind(parser);
		MappedQueue<JsonEvent, JsonEvent> timeGuesser = new MappedQueue<>(
				new ValueMapper<JsonEvent, JsonEvent>() {

					@Override
					public JsonEvent map(JsonEvent value) {
						String time = value.get("time");
						if (time != null) {
							Calendar now = Calendar.getInstance();
							String millies = time.split(",")[1];
							now.set(Calendar.MILLISECOND,
									Integer.parseInt(millies));
							String[] parts = time.split(",")[0].split(":");
							now.set(Calendar.HOUR_OF_DAY,
									Integer.parseInt(parts[0]));
							now.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
							now.set(Calendar.SECOND, Integer.parseInt(parts[2]));
							value.put("time",
									String.valueOf(now.getTimeInMillis()));
						}
						return value;
					}
				});
		parser.getQueue().bind(timeGuesser);

		FileAppender<JsonEvent> writer = new FileAppender<JsonEvent>(Files
				.createTempFile(null, null).toAbsolutePath());
		parser.getQueue().bind(writer);

		Path inputFile = Files.createTempFile(null, null);
		shipper.readOnce(inputFile);
		Files.write(inputFile,
				"123 INFO [dummy] message".getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.APPEND);
		Files.write(inputFile,
				" message to be combined".getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.APPEND);
	}

	@Test
	public void testSimpleJoin() throws IOException {
		RegExCombinator combinator = new RegExCombinator(
				"^\\s+.*|^Caused by: .*");
		final Collector<String> collector = new Collector<>();
		combinator.getQueue().bind(collector);

		combinator.feed("new event");
		combinator.feed("other new event");
		combinator.feed(" second line of new event");
		combinator.feed(" third line of new event");
		combinator.feed("yet another new event");

		final Collector<String> expected = new Collector<>();
		expected.feed("new event");
		expected.feed("other new event" + System.lineSeparator()
				+ " second line of new event" + System.lineSeparator()
				+ " third line of new event");
		// "yet another new event" is still pending.

		Assert.assertEquals(expected, collector);

		Timer t = new Timer(true);
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				expected.feed("yet another new event");
				Assert.assertEquals(expected, collector);
			}
		}, 1000);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}