package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.AggregateFunctionConfig;
import yield.config.ConfigReader;
import yield.config.TypedYielder;
import yield.core.Aggregator;
import yield.core.Producer;
import yield.core.Query;
import yield.core.Window;

public class Count extends AggregateFunctionConfig<Object> {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Calculates rate or number of events.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(final String args,
			Map<String, TypedYielder> context) {
		Query<Integer> query = new Query<>(
				context.get(ConfigReader.LAST_SOURCE).yielder).within(
				new Producer<Window<Object>>() {

					@Override
					public Window<Object> make() {
						return getWindow(args);
					}
				}, new Producer<Aggregator<Object, Integer>>() {

					@Override
					public Aggregator<Object, Integer> make() {
						return new yield.core.aggregators.Count<>();
					}
				});
		return wrapResultingYielder(query.getQueue());
	}

	@Override
	protected String getResultEventType() {
		return Integer.class.getName();
	}
}
