package yield.config.function;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;

import yield.config.ConfigReader;
import yield.config.FunctionConfig;
import yield.config.ParameterMap;
import yield.config.ParameterMap.Param;
import yield.config.ShortDocumentation;
import yield.config.TypedYielder;
import yield.config.function.where.Expr;
import yield.config.function.where.FilterParser;
import yield.core.EventType;
import yield.core.MappedQueue;
import yield.core.Yielder;
import yield.json.JsonEvent;
import yield.json.TimestampQueue;

@ShortDocumentation(text = "Conserts texts to a timestamp.")
public class Timestamp extends FunctionConfig {
	private static enum Parameters implements Param {
		@ShortDocumentation(text = "Format pattern for SimpleDateFormat, or the constants =ISO8601, =EPOCH-MILLISECONDS.")
		pattern {
			@Override
			public String getDefault() {
				return "=ISO8601";
			}
		},
		@ShortDocumentation(text = "Expression to build text to parse as time.")
		source {
			@Override
			public String getDefault() {
				return "timestamp";
			}
		},
		@ShortDocumentation(text = "Values to assume in case of successful, but ambiguous parsing. ISO-8601 date or NOW which always means the time at parsing.")
		base {
			@Override
			public String getDefault() {
				return "NOW";
			}
		},
		@ShortDocumentation(text = "Locale for language dependent elements such as month names.")
		locale {
			@Override
			public String getDefault() {
				return Locale.getDefault().toLanguageTag();
			}
		};
	}

	@Override
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		ParameterMap<Parameters> parameters = parseArguments(args,
				Parameters.class);
		String pattern = parameters.getString(Parameters.pattern);
		Locale locale = Locale.forLanguageTag(parameters
				.getString(Parameters.locale));
		String inputValue = parameters.getString(Parameters.source);
		Expr source = new FilterParser().buildExpression(inputValue);
		Calendar base;
		if ("NOW".equals(parameters.getString(Parameters.base))) {
			base = null;
		} else {
			base = DatatypeConverter.parseDateTime(parameters
					.getString(Parameters.base));
		}

		MappedQueue<JsonEvent, JsonEvent> results = new MappedQueue<>(
				new TimestampQueue(pattern, locale, base, source),
				JsonEvent.class, JsonEvent.class);
		Yielder<JsonEvent> input = getYielderTypesafe(JsonEvent.class,
				ConfigReader.LAST_SOURCE, context);
		input.bind(results);
		return TypedYielder.wrap(new EventType(JsonEvent.class),
				results.getQueue());
	}

}
