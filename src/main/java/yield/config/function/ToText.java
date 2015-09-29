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
import yield.json.Template;

@ShortDocumentation(text = "Converts to text.")
public class ToText extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Yielder<? extends Object> yielder;
		if (args.isEmpty()) {
			Yielder<Object> input = getYielderTypesafe(Object.class,
					ConfigReader.LAST_SOURCE, context);
			MappedQueue<Object, String> mapping = new MappedQueue<>(
					new ValueMapper<Object, String>() {
						@Override
						public String map(Object value) {
							return value.toString();
						}
					}, Object.class, String.class);
			input.bind(mapping);
			yielder = mapping.getQueue();
		} else {
			TypedYielder typedInput = context.get(ConfigReader.LAST_SOURCE);
			if (typedInput.type.equals(JsonEvent.class.getName())) {
				final Template template = new Template(args);
				MappedQueue<JsonEvent, String> mapping = new MappedQueue<>(
						new ValueMapper<JsonEvent, String>() {
							@Override
							public String map(JsonEvent value) {
								return template.apply(value);
							}
						}, JsonEvent.class, String.class);
				getYielderTypesafe(JsonEvent.class, ConfigReader.LAST_SOURCE,
						context).bind(mapping);
				yielder = mapping.getQueue();
			} else {
				throw new IllegalArgumentException("Only "
						+ JsonEvent.class.getName()
						+ " is a valid input type when specifiying a template.");
			}
		}
		return wrapResultingYielder(yielder);
	}

	@Override
	@Nonnull
	public EventType getResultEventType() {
		return new EventType(String.class);
	}
}