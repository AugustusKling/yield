package yield.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals.LongLiteral;
import org.codehaus.jparsec.Terminals.StringLiteral;
import org.codehaus.jparsec.functors.Map2;
import org.codehaus.jparsec.functors.Pair;

import yield.core.Yielder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a functions in yield's configuration file.
 */
public abstract class FunctionConfig {
	protected Parser<Pair<String, String>> ARGUMENT = Parsers
			.tuple(Scanners.IDENTIFIER,
					Scanners.string("=")
							.next(StringLiteral.DOUBLE_QUOTE_TOKENIZER.or(LongLiteral.TOKENIZER
									.map(new org.codehaus.jparsec.functors.Map<Long, String>() {
										@Override
										public String map(Long from) {
											return from.toString();
										}
									}))));
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
	protected <Parameter extends Enum<Parameter>> Map<Parameter, String> parseArguments(
			String args, final Class<Parameter> parameters) {
		return Parsers
				.sequence(
						// Positional parameters.
						StringLiteral.DOUBLE_QUOTE_TOKENIZER
								.notFollowedBy(Scanners.string("="))
								.sepBy(Scanners.string(" ")).optional(),
						// Named parameters.
						Scanners.string(" ").optional().next(ARGS).optional(),
						new Map2<List<String>, List<Pair<String, String>>, Map<Parameter, String>>() {

							@Override
							public Map<Parameter, String> map(
									List<String> positional,
									List<Pair<String, String>> named) {
								Parameter[] enumConstants = parameters
										.getEnumConstants();
								if (positional.size() > enumConstants.length) {
									throw new IllegalArgumentException(
											"More positional parameters provided than defined by the function.");
								}
								// Add positional parameters.
								Map<Parameter, String> ret = new HashMap<>();
								for (int pos = 0; pos < positional.size(); pos++) {
									ret.put(enumConstants[pos],
											positional.get(pos));
								}
								// Add named parameters.
								for (Pair<String, String> p : named) {
									if (ret.containsKey(p.a)) {
										throw new IllegalArgumentException(
												"Parameter " + p.a
														+ " specified twice.");
									} else {
										try {
											ret.put(Enum.valueOf(parameters,
													p.a), p.b);
										} catch (IllegalArgumentException e) {
											throw new IllegalArgumentException(
													"Unknown parameter " + p.a,
													e);
										}
									}
								}
								return ret;
							}

						}).parse(args);
	}

	@Override
	public String toString() {
		if (shortDescription().isEmpty()) {
			return getClass().getName();
		} else {
			return getClass().getName() + " " + shortDescription();
		}
	}

	@Nonnull
	protected String shortDescription() {
		return "";
	}
}
