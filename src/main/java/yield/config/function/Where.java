package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.config.function.where.Expr;
import yield.config.function.where.FilterParser;
import yield.core.EventType;
import yield.core.Filter;
import yield.core.Yielder;
import yield.json.JsonEvent;

@ShortDocumentation(text = "Filters JSON queue.")
public class Where extends FunctionConfig {
	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {

		Yielder<JsonEvent> yielder = getYielderTypesafe(JsonEvent.class,
				ConfigReader.LAST_SOURCE, context);
		Filter<JsonEvent> filter = parse(args);
		yielder.bind(filter);
		return wrapResultingYielder(filter.getQueue());
	}

	/**
	 * @param args
	 *            Function arguments.
	 * @return Queue filter.
	 */
	protected Filter<JsonEvent> parse(String args) {
		final Expr expr = new FilterParser().buildExpression(args);

		return new Filter<JsonEvent>() {
			@Override
			protected boolean matches(JsonEvent e) {
				Object res = expr.apply(e).getValue();
				if (res == null) {
					throw new IllegalArgumentException(
							"Filter evaluation did not yield an undefined result.");
				} else if (res instanceof Boolean) {
					return ((Boolean) res).booleanValue();
				} else {
					throw new IllegalArgumentException(
							"Filter evaluation did not yield a Boolean result.");
				}
			}

			@Override
			@Nonnull
			public EventType getInputType() {
				return new EventType(JsonEvent.class);
			}
		};

	}

	@Override
	@Nonnull
	public EventType getResultEventType() {
		return new EventType(JsonEvent.class);
	}
}