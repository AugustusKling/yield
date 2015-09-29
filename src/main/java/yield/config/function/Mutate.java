package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.config.function.where.Expr;
import yield.config.function.where.ExprLiteral;
import yield.config.function.where.FilterParser;
import yield.core.EventType;
import yield.core.MappedQueue;
import yield.core.ValueMapper;
import yield.core.Yielder;
import yield.json.JsonEvent;

@ShortDocumentation(text = "Sets or removes properties of JSON events based on templates.")
public class Mutate extends FunctionConfig {
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
		final Expr template;
		if (mode.equals("+")) {
			template = new FilterParser().buildExpression(parts[1]);
		} else {
			template = null;
		}

		Yielder<JsonEvent> yielder = getYielderTypesafe(JsonEvent.class,
				ConfigReader.LAST_SOURCE, context);
		MappedQueue<JsonEvent, JsonEvent> mapper = new MappedQueue<>(
				new ValueMapper<JsonEvent, JsonEvent>() {

					@Override
					public JsonEvent map(JsonEvent value) {
						JsonEvent copy = new JsonEvent(value);
						if (mode.equals("+")) {
							@SuppressWarnings("null")
							ExprLiteral exprLiteral = template.apply(value);
							if (!exprLiteral.isUnknown()) {
								Object evaluated = exprLiteral.getValue();
								if (evaluated != null) {
									evaluated = evaluated.toString();
								}
								copy.put(fieldName, (String) evaluated);
							} else {
								// Discard unknown values for consistency with
								// JavaScript's stringify.
								copy.remove(fieldName);
							}
						} else {
							copy.remove(fieldName);
						}
						return copy;
					}
				}, JsonEvent.class, JsonEvent.class);
		yielder.bind(mapper);
		return wrapResultingYielder(mapper.getQueue());
	}

	@Override
	@Nonnull
	protected EventType getResultEventType() {
		return new EventType(JsonEvent.class);
	}
}
