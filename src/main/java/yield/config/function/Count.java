package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.AggregateFunctionConfig;
import yield.config.ConfigReader;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.core.Aggregator;
import yield.core.EventType;
import yield.core.Producer;
import yield.core.Query;
import yield.core.Window;

@ShortDocumentation(text = "Calculates rate or number of events.")
public class Count extends AggregateFunctionConfig<Object> {
	@Override
	@Nonnull
	public TypedYielder getSource(final String args,
			Map<String, TypedYielder> context) {
		Query<Integer> query = new Query<>(getYielderTypesafe(Object.class,
				ConfigReader.LAST_SOURCE, context)).within(
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
	@Nonnull
	protected EventType getResultEventType() {
		return new EventType(Integer.class);
	}
}
