package yield.config;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals.LongLiteral;
import org.codehaus.jparsec.Terminals.StringLiteral;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Pair;
import org.codehaus.jparsec.pattern.Patterns;

import yield.config.ParameterMap.Param;
import yield.core.Yielder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a functions in yield's configuration file.
 */
public abstract class FunctionConfig {
	/**
	 * Word character or underscores followed by numbers, word characters, dots
	 * or underscores.
	 */
	private static final Parser<String> IDENTIFIER = Scanners.pattern(
			Patterns.regex(Pattern.compile("(?:\\w|_)(?:\\w|[0-9_.])*",
					Pattern.UNICODE_CHARACTER_CLASS)), "identifier").source();

	/**
	 * Argument name, equals sign and argument value.
	 */
	protected Parser<Pair<String, String>> ARGUMENT = Parsers
			.tuple(IDENTIFIER.or(StringLiteral.DOUBLE_QUOTE_TOKENIZER),
					Scanners.string("=")
							.next(StringLiteral.DOUBLE_QUOTE_TOKENIZER.or(LongLiteral.TOKENIZER
									.map(new org.codehaus.jparsec.functors.Map<Long, String>() {
										@Override
										public String map(Long from) {
											return from.toString();
										}
									}))));

	/**
	 * Any number of {@link #ARGUMENT}s.
	 */
	protected Parser<List<Pair<String, String>>> ARGS = ARGUMENT.sepBy(Scanners
			.string(" "));

	/**
	 * Expected event type. Might get more restrictive over time.
	 * 
	 * @return Type of yielded events. For example
	 *         {@code java.util.Map<java.lang.String,java.lang.Object>}
	 */
	protected String getResultEventType() {
		return "java.lang.Object";
	}

	@Nonnull
	protected TypedYielder wrapResultingYielder(
			Yielder<? extends Object> yielder) {
		return TypedYielder.wrap(getResultEventType(), yielder);
	}

	/**
	 * Returns a {@link Yielder} after verifying it matches the required type.
	 * 
	 * @param requiredType
	 *            Required event type of {@link Yielder}.
	 * @param yielderName
	 *            Identifier in given {@code context}.
	 * @param context
	 *            All known {@link Yielder}s.
	 * @return Matching {@link Yielder} from {@code context}.
	 */
	@SuppressWarnings("unchecked")
	@Nonnull
	protected <RequiredType> Yielder<RequiredType> getYielderTypesafe(
			String requiredType, String yielderName,
			Map<String, TypedYielder> context) {
		TypedYielder typedYielder = context.get(yielderName);
		if (typedYielder.type.equals(requiredType)) {
			return (Yielder<RequiredType>) typedYielder.yielder;
		} else {
			throw new RuntimeException("Requires " + requiredType + " but "
					+ yielderName + " yields events of type "
					+ typedYielder.type);
		}
	}

	/**
	 * @see #getYielderTypesafe(String, String, Map)
	 */
	@Nonnull
	protected <RequiredType> Yielder<RequiredType> getYielderTypesafe(
			Class<RequiredType> requiredType, String yielderName,
			Map<String, TypedYielder> context) {
		return getYielderTypesafe(requiredType.getName(), yielderName, context);
	}

	/**
	 * Invokes the function to prepare its yielder.
	 * 
	 * @param args
	 *            Function argument string. This is the text after the function
	 *            name.
	 * @param context
	 *            Currently available yielders.
	 * @return Yielder of this function.
	 */
	@Nonnull
	public abstract TypedYielder getSource(String args,
			Map<String, TypedYielder> context);

	/**
	 * Converts arguments to JSON.
	 * 
	 * @param args
	 *            Function argument string. This is the text after the function
	 *            name.
	 * @return Parsed arguments as key-value mapping.
	 */
	protected ObjectNode parseArguments(String args) {
		List<Pair<String, String>> res = ARGS.parse(args);

		ObjectNode config = new ObjectMapper().createObjectNode();
		for (Pair<String, String> p : res) {
			config.put(p.a, p.b);
		}
		return config;
	}

	/**
	 * Parses function parameters.
	 * 
	 * @param args
	 *            Function parameter string. This is the text after the function
	 *            name in the configuration file.
	 * @param parameters
	 *            Enumeration containing allowed parameters. Order is
	 *            significant to correlate positional parameters' values.
	 * @return Parsed arguments as key-value mapping.
	 */
	protected <Parameter extends Enum<Parameter> & Param> ParameterMap<Parameter> parseArguments(
			String args, final Class<Parameter> parameters) {
		return Parsers
				.sequence(
						// Positional parameters.
						StringLiteral.DOUBLE_QUOTE_TOKENIZER
								.notFollowedBy(Scanners.string("="))
								.sepBy(Scanners.string(" ")).optional(),
						// Named parameters.
						Scanners.string(" ").optional().next(ARGS).optional(),
						new Map2<List<String>, List<Pair<String, String>>, ParameterMap<Parameter>>() {

							@Override
							public ParameterMap<Parameter> map(
									List<String> positional,
									List<Pair<String, String>> named) {
								return new ParameterMap<>(parameters,
										positional, named);
							}

						}).parse(args);
	}

	@Override
	public String toString() {
		ShortDocumentation shortDocumentation = getClass().getAnnotation(
				ShortDocumentation.class);
		if (shortDocumentation == null) {
			// Annotation is not present.
			return getClass().getName();
		} else {
			String docString = shortDocumentation.text();
			if (docString == null || docString.isEmpty()) {
				return getClass().getName();
			} else {
				return getClass().getName() + " (" + docString + ")";
			}
		}
	}

	/**
	 * @return Available parameters or {@code null} if function does not have
	 *         parameters.
	 */
	@Nullable
	public <Parameter extends Enum<Parameter> & Param> Class<? extends Param> getParameters() {
		return null;
	};
}
