package yield.config.function;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.TypedYielder;
import yield.core.queues.DelayedQueue;

public class Delay extends FunctionConfig {

	private String type;

	@Override
	@Nonnull
	protected String shortDescription() {
		return "Delays events by the given time.";
	}

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
			TypedYielder input = context.get(ConfigReader.LAST_SOURCE);
			this.type = input.type;
			DelayedQueue<Object> delayed = new DelayedQueue<>(value, unit);
			input.yielder.bind(delayed);
			return wrapResultingYielder(delayed);
		}
	}

	@Override
	protected String getResultEventType() {
		return this.type;
	}
}
