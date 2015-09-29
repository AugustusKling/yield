package yield.config.function;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.EventType;
import yield.core.Yielder;
import yield.core.queues.DelayedQueue;

@ShortDocumentation(text = "Delays events by the given time.")
public class Delay extends FunctionConfig {
	@Nonnull
	private EventType type = EventType.ALL;

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		Matcher matcher = Pattern.compile("\\s*(?<value>\\d+) (?<unit>\\w+)$")
				.matcher(args);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(
					"Require delay to be given as value with unit.");
		} else {
			long value = Long.parseLong(matcher.group("value"));
			String unitGiven = matcher.group("unit");
			TimeUnit unit;
			try {
				unit = TimeUnit.valueOf(unitGiven.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Unit " + unitGiven
						+ " not supported. Valid are: "
						+ Arrays.asList(TimeUnit.values()));
			}
			Yielder<Object> input = getYielderTypesafe(Object.class,
					ConfigReader.LAST_SOURCE, context);
			this.type = input.getOutputType();
			DelayedQueue<Object> delayed = new DelayedQueue<>(value, unit);
			input.bind(delayed);
			return wrapResultingYielder(delayed);
		}
	}

	@Override
	@Nonnull
	protected EventType getResultEventType() {
		return this.type;
	}
}
