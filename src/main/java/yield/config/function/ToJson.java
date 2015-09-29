package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventType;
import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.core.Yielder;
import yield.json.JsonEvent;

@ShortDocumentation(text = "Parses as JSON.")
public class ToJson extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Yielder<String> yielder = getYielderTypesafe(String.class,
				ConfigReader.LAST_SOURCE, context);
		MappedQueue<String, JsonEvent> input = new MappedQueue<>(
				new ValueMapper<String, JsonEvent>() {

					@Override
					public JsonEvent map(String value) {
						return new JsonEvent(value);
					}
				}, String.class, JsonEvent.class);
		yielder.bind(input);
		return wrapResultingYielder(input.getQueue());
	}

	@Override
	@Nonnull
	public EventType getResultEventType() {
		return new EventType(JsonEvent.class);
	}

}