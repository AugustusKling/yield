package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.core.Yielder;
import yield.json.JsonEvent;

public class ToJson extends FunctionConfig {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Parses as JSON.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Yielder<String> yielder = getYielderTypesafe(String.class.getName(),
				ConfigReader.LAST_SOURCE, context);
		MappedQueue<String, JsonEvent> input = new MappedQueue<>(
				new ValueMapper<String, JsonEvent>() {

					@Override
					public JsonEvent map(String value) {
						return new JsonEvent(value);
					}
				});
		yielder.bind(input);
		return wrapResultingYielder(input.getQueue());
	}

	@Override
	public String getResultEventType() {
		return JsonEvent.class.getName();
	}

}