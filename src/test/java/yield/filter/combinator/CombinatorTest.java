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
import yield.input.file.FileInput;
import yield.json.JsonEvent;
import yield.json.JsonGrok;
import yield.output.file.FileAppender;
import yield.test.Collector;

public class CombinatorTest {
	@Test
	public void testLogFiles() throws IOException, InterruptedException {
		RegExCombinator combinator = new RegExCombinator(
				"^\\s+.*|^\\s*$|^Caused by: .*");
		Path inputFile = Files.createTempFile(null, null);
		FileInput fileInput = new FileInput(inputFile);
		fileInput.getQueue().bind(combinator);

		MappedQueue<String, JsonEvent> input = new MappedQueue<>(
				new ValueMapper<String, JsonEvent>() {

					@Override
					public JsonEvent map(String value) {
						return new JsonEvent(value);
					}
				}, String.class, JsonEvent.class);
		combinator.getQueue().bind(input);
		MappedQueue<JsonEvent, JsonEvent> parser = new MappedQueue<>(
				new JsonGrok("message",
						"^(?<time>[^ ]+) (?<level>\\w+)\\s+\\[(?<module>[^\\]]+)\\] (?<message>.+)$"),
				JsonEvent.class, JsonEvent.class);
		input.getQueue().bind(parser);

		// TODO Move to appropriate place.
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
				}, JsonEvent.class, JsonEvent.class);
		parser.getQueue().bind(timeGuesser);

		Collector<JsonEvent> colCombined = new Collector<>();
		parser.getQueue().bind(colCombined);

		MappedQueue<JsonEvent, String> toText = new MappedQueue<>(
				new ValueMapper<JsonEvent, String>() {
					@Override
					public String map(JsonEvent value) {
						return value.toString();
					}
				}, JsonEvent.class, String.class);
		parser.getQueue().bind(toText);

		FileAppender<String> writer = new FileAppender<String>(Files
				.createTempFile(null, null).toAbsolutePath());
		toText.getQueue().bind(writer);

		Files.write(inputFile, "20:51:53,123 INFO [dummy] message\n"
				.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
		Files.write(inputFile,
				" message to be combined".getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.APPEND);
		fileInput.read(true, false, StandardCharsets.UTF_8);

		// Wait to allow file writing and reading to happen.
		Thread.sleep(1500);

		Assert.assertTrue(colCombined.get(0).get("level").equals("INFO"));
		Assert.assertTrue(colCombined.get(0).get("message")
				.contains("to be combined"));
	}

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