package yield.config.function;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import yield.config.ConfigReader;
import yield.config.TypedYielder;
import yield.config.function.where.Expr;
import yield.config.function.where.FilterParser;
import yield.core.EventQueue;
import yield.core.EventType;
import yield.core.Yielder;
import yield.json.JsonEvent;
import yield.test.Collector;

public class WhereTest {

	@Test
	public void parse() {
		String input = "message contains \"test\" and (lower(severity coalesce x)=\"error\" or x<=\"g\")";

		Expr res = new FilterParser().buildExpression(input);
		// Print AST.
		// System.out.println(res);

		JsonEvent case1 = new JsonEvent();
		case1.put("message", "abcdeftestz");
		case1.put("x", "error");
		Assert.assertTrue(Boolean.TRUE.equals(res.apply(case1).getValue()));

		JsonEvent case2 = new JsonEvent();
		case2.put("message", "abcdeftestz");
		case2.put("severity", "warn");
		case2.put("x", "b");
		Assert.assertTrue(Boolean.TRUE.equals(res.apply(case2).getValue()));

		JsonEvent case3 = new JsonEvent();
		case3.put("message", "abcdeftestz");
		case3.put("severity", "warn");
		case3.put("x", "z");
		Assert.assertFalse(Boolean.TRUE.equals(res.apply(case3).getValue()));
	}

	@Test
	public void positiveEquals() throws Exception {
		Where where = new Where();

		EventQueue<JsonEvent> inputQueue = new EventQueue<>(JsonEvent.class);
		TypedYielder input = TypedYielder.wrap(new EventType(JsonEvent.class),
				inputQueue);
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, input);

		TypedYielder positiveEqualsFilter = where.getSource("x=\"match test\"",
				context);

		Collector<JsonEvent> remaining = new Collector<>();
		Yielder<JsonEvent> filtered = positiveEqualsFilter
				.getTypesafe(new EventType(JsonEvent.class));
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

		EventQueue<JsonEvent> inputQueue = new EventQueue<>(JsonEvent.class);
		TypedYielder input = TypedYielder.wrap(new EventType(JsonEvent.class),
				inputQueue);
		Map<String, TypedYielder> context = new HashMap<>();
		context.put(ConfigReader.LAST_SOURCE, input);

		TypedYielder negEqualsFilter = where.getSource(
				"not (x=\"match test\")", context);

		Collector<JsonEvent> remaining = new Collector<>();
		Yielder<JsonEvent> filtered = negEqualsFilter
				.getTypesafe(new EventType(JsonEvent.class));
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

	@Test
	public void matches() {
		JsonEvent context = new JsonEvent();
		context.put("testvalue", "this is a test");
		context.put("testpattern", ".*test.*");
		context.put("testpattern2", ".*nonmatch.*");

		Expr e = new FilterParser()
				.buildExpression("\"abcdef\" matches \"abc.*\"");
		Assert.assertTrue((Boolean) e.apply(context).getValue());

		Expr e2 = new FilterParser()
				.buildExpression("testvalue matches \"this.*\"");
		Assert.assertTrue((Boolean) e2.apply(context).getValue());
		Expr e3 = new FilterParser()
				.buildExpression("testvalue matches \"that.*\"");
		Assert.assertFalse((Boolean) e3.apply(context).getValue());

		Expr e4 = new FilterParser()
				.buildExpression("\"testvalue\" matches testpattern");
		Assert.assertTrue((Boolean) e4.apply(context).getValue());
		Expr e5 = new FilterParser()
				.buildExpression("\"testvalue\" matches testpattern2");
		Assert.assertFalse((Boolean) e5.apply(context).getValue());

		Expr e6 = new FilterParser()
				.buildExpression("testvalue matches testpattern");
		Assert.assertTrue((Boolean) e6.apply(context).getValue());
		Expr e7 = new FilterParser()
				.buildExpression("testvalue matches testpattern2");
		Assert.assertFalse((Boolean) e7.apply(context).getValue());
	}
}
