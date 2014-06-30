package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.Filter;
import yield.core.Yielder;
import yield.json.JsonEvent;

public class Where extends FunctionConfig {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Filters JSON queue.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Yielder<JsonEvent> yielder = getYielderTypesafe(
				JsonEvent.class.getName(), ConfigReader.LAST_SOURCE, context);
		Filter<JsonEvent> filter;
		final boolean isPositive = !args.startsWith("not ");
		if (!isPositive) {
			args = args.substring(4);
		}
		if (args.contains("=")) {
			String[] criterion = args.split("\\s*=\\s*", 2);
			if (criterion.length != 2) {
				throw new IllegalArgumentException("Cannot parse arguments.");
			} else {
				final String key;
				if (criterion[0].startsWith("\"")) {
					key = criterion[0].substring(1).replaceAll("\"$", "")
							.trim();
				} else {
					key = criterion[0];
				}
				final String filterValue;
				if (criterion[1].startsWith("\"")) {
					filterValue = criterion[1].substring(1)
							.replaceAll("\"$", "").trim();
				} else {
					filterValue = criterion[1];
				}
				filter = new Filter<JsonEvent>() {
					@Override
					protected boolean matches(JsonEvent e) {
						String value = e.get(key);
						return (value != null && value.equals(filterValue)) == isPositive;
					}
				};
			}
		} else {
			final String criterion = args;
			filter = new Filter<JsonEvent>() {
				@Override
				protected boolean matches(JsonEvent e) {
					String value = e.get(criterion.trim());
					return (value != null) == isPositive;
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