package yield.config.function;

import java.util.Map;

import javax.annotation.Nonnull;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.functors.Pair;

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
		Parser<Filter<JsonEvent>> filter = Parsers
				.or(Scanners
						.string("not ")
						.next(ARGUMENT)
						.map(new org.codehaus.jparsec.functors.Map<Pair<String, String>, Filter<JsonEvent>>() {

							@Override
							public Filter<JsonEvent> map(
									final Pair<String, String> from) {
								return new Filter<JsonEvent>() {

									@Override
									protected boolean matches(JsonEvent e) {
										String value = e.get(from.a);
										return (value != null && value
												.equals(from.b)) == false;
									}
								};
							}
						}),
						ARGUMENT.map(new org.codehaus.jparsec.functors.Map<Pair<String, String>, Filter<JsonEvent>>() {

							@Override
							public Filter<JsonEvent> map(
									final Pair<String, String> from) {
								return new Filter<JsonEvent>() {

									@Override
									protected boolean matches(JsonEvent e) {
										String value = e.get(from.a);
										return value != null
												&& value.equals(from.b);
									}
								};
							}
						}));
		return filter.parse(args);

	}

	@Override
	public String getResultEventType() {
		return JsonEvent.class.getName();
	}
}