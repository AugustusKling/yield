package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventType;
import yield.core.MappedQueue;
import yield.core.Yielder;
import yield.json.JsonEvent;
import yield.json.JsonGrok;

@ShortDocumentation(text = "Uses regular expression with groups against value from JSON event. Matched groups define new properties for the yielded event.")
public class Grok extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Yielder<JsonEvent> input = getYielderTypesafe(JsonEvent.class,
				ConfigReader.LAST_SOURCE, context);
		String[] parts = args.split(" ", 2);
		if (parts.length != 2) {
			throw new IllegalArgumentException(
					"Expected property name followed by pattern with named groups.");
		}
		MappedQueue<JsonEvent, JsonEvent> grok = new MappedQueue<>(
				new JsonGrok(parts[0], parts[1]), JsonEvent.class,
				JsonEvent.class);
		input.bind(grok);
		return wrapResultingYielder(grok.getQueue());
	}

	@Override
	@Nonnull
	protected EventType getResultEventType() {
		return new EventType(JsonEvent.class);
	}
}
