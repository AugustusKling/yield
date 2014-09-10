package yield.json;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import yield.config.ConfigReader;
import yield.config.TypedYielder;
import yield.config.function.Timestamp;
import yield.core.EventQueue;
import yield.core.Yielder;
import yield.test.Collector;

public class DateTest {
	@Test
	public void compliant() {
		EventQueue<JsonEvent> inputQueue = new EventQueue<>();
		TypedYielder input = TypedYielder.wrap(JsonEvent.class.getName(),
				inputQueue);
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, input);

		TypedYielder ts = new Timestamp().getSource(
				"pattern=\"=ISO8601\" source=\"time\" locale=\"en\"", context);

		Collector<JsonEvent> c = new Collector<>();
		Yielder<JsonEvent> y = ts.getTypesafe(JsonEvent.class.getName());
		y.bind(c);

		JsonEvent in = new JsonEvent();
		in.put("time", "2010-01-01T12:00:00Z");
		inputQueue.feed(in);
		JsonEvent out = c.get(0);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(out.getObject().get("timestamp").asLong());
		Assert.assertEquals(2010, cal.get(Calendar.YEAR));
	}

	@Test
	public void syslog() {
		EventQueue<JsonEvent> inputQueue = new EventQueue<>();
		TypedYielder input = TypedYielder.wrap(JsonEvent.class.getName(),
				inputQueue);
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, input);

		TypedYielder ts = new Timestamp().getSource(
				"pattern=\"MMM d HH:mm:ss\" source=\"time\" locale=\"en\"",
				context);

		Collector<JsonEvent> c = new Collector<>();
		Yielder<JsonEvent> y = ts.getTypesafe(JsonEvent.class.getName());
		y.bind(c);
		JsonEvent in2 = new JsonEvent();
		in2.put("time", "Aug 31 14:33:38");
		inputQueue.feed(in2);
		JsonEvent out2 = c.get(0);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(out2.getObject().get("timestamp").asLong());
		Assert.assertEquals(31, cal2.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(Calendar.getInstance().get(Calendar.YEAR),
				cal2.get(Calendar.YEAR));
	}

	@Test
	public void epochMilliseconds() {
		EventQueue<JsonEvent> inputQueue = new EventQueue<>();
		TypedYielder input = TypedYielder.wrap(JsonEvent.class.getName(),
				inputQueue);
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, input);

		TypedYielder ts = new Timestamp()
				.getSource(
						"pattern=\"=EPOCH-MILLISECONDS\" source=\"time\" locale=\"en\"",
						context);

		Collector<JsonEvent> c = new Collector<>();
		Yielder<JsonEvent> y = ts.getTypesafe(JsonEvent.class.getName());
		y.bind(c);
		JsonEvent in2 = new JsonEvent();
		in2.put("time", "1404125501001");
		inputQueue.feed(in2);
		Calendar expected = Calendar.getInstance();
		expected.setTimeInMillis(1404125501001L);
		JsonEvent out2 = c.get(0);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(out2.getObject().get("timestamp").asLong());
		Assert.assertEquals(expected.getTimeInMillis(), cal2.getTimeInMillis());
	}
}
