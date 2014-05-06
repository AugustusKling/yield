package yield.config.function;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.AggregateFunctionConfig;
import yield.config.ConfigReader;
import yield.config.TypedYielder;
import yield.core.Aggregator;
import yield.core.Producer;
import yield.core.Query;
import yield.core.Window;
import yield.core.Yielder;

public class Average extends AggregateFunctionConfig<Number> {
	@Override
	@Nonnull
	protected String shortDescription() {
		return "Arithmetic mean of numeric values.";
	}

	@Override
	@Nonnull
	public TypedYielder getSource(final String args,
			Map<String, TypedYielder> context) {
		Yielder<Number> input = getYielderTypesafe(Number.class,
				ConfigReader.LAST_SOURCE, context);
		Query<Number> query = new Query<Number>(input).within(
				new Producer<Window<Number>>() {
					@Override
					public Window<Number> make() {
						return getWindow(args);
					}
				}, new Producer<Aggregator<Number, Number>>() {

					@Override
					public Aggregator<Number, Number> make() {
						return new Aggregator<Number, Number>() {

							@Override
							protected void aggregate(Iterable<Number> events) {
								int windowSize = 0;
								BigDecimal total = new BigDecimal(0);
								for (Number current : events) {
									total = total.add(new BigDecimal(current
											.toString()));
									windowSize = windowSize + 1;
								}
								if (windowSize == 0) {
									getQueue().feed(Double.NaN);
								} else {
									getQueue().feed(
											total.divide(new BigDecimal(
													windowSize)));
								}
							}
						};
					}
				});
		return wrapResultingYielder(query.getQueue());
	}

	@Override
	protected String getResultEventType() {
		return Number.class.getName();
	}
}
