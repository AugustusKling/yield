package yield.config.function;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.Filter;
import yield.core.Yielder;
import yield.json.JsonEvent;

import com.fasterxml.jackson.databind.JsonNode;

public class Where extends FunctionConfig {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Filters JSON queue.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(final String args,
			Map<String, TypedYielder> context) {
		Yielder<JsonEvent> yielder = getYielderTypesafe(
				JsonEvent.class.getName(), ConfigReader.LAST_SOURCE, context);
		Filter<JsonEvent> filter;
		if (args.contains("=")) {
			final Entry<String, JsonNode> criterion = parseArguments(args)
					.fields().next();
			filter = new Filter<JsonEvent>() {

				@Override
				protected boolean matches(JsonEvent e) {
					String value = e.get(criterion.getKey().trim());
					return value != null
							&& value.equals(criterion.getValue().textValue()
									.trim());
				}
			};
		} else {
			filter = new Filter<JsonEvent>() {

				@Override
				protected boolean matches(JsonEvent e) {
					String value = e.get(args.trim());
					return value != null;
				}
			};
		}
		yielder.bind(filter);
		return wrapResultingYielder(filter.getQueue());
	}

	@Override
	public String getResultEventType() {
		return JsonEvent.class.getName();
	}
}