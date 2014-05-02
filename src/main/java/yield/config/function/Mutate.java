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
import yield.json.Template;

public class Mutate extends FunctionConfig {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Sets or removes properties of JSON events based on templates.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		final String[] parts = args.split(" ", 2);
		final String mode = parts[0].substring(0, 1);
		final String fieldName = parts[0].substring(1);
		if (!(mode.equals("+") || mode.equals("-"))) {
			throw new IllegalArgumentException(
					"Cannot find operation mode. First character is mode and either + or - is required.");
		} else if (mode.equals("+") && parts.length < 2) {
			throw new IllegalArgumentException("Set mode requires value.");
		} else if (mode.equals("-") && parts.length != 1) {
			throw new IllegalArgumentException(
					"Delete mode cannot have an argument.");
		}
		final Template template;
		if (mode.equals("+")) {
			template = new Template(parts[1]);
		} else {
			template = new Template("");
		}

		Yielder<JsonEvent> yielder = getYielderTypesafe(JsonEvent.class,
				ConfigReader.LAST_SOURCE, context);
		MappedQueue<JsonEvent, JsonEvent> mapper = new MappedQueue<>(
				new ValueMapper<JsonEvent, JsonEvent>() {

					@Override
					public JsonEvent map(JsonEvent value) {
						JsonEvent copy = new JsonEvent(value);
						if (mode.equals("+")) {
							copy.put(fieldName, template.apply(value));
						} else {
							copy.remove(fieldName);
						}
						return copy;
					}
				});
		yielder.bind(mapper);
		return wrapResultingYielder(mapper.getQueue());
	}

	@Override
	protected String getResultEventType() {
		return JsonEvent.class.getName();
	}
}
