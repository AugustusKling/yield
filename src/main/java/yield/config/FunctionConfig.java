package yield.config;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals.LongLiteral;
import org.codehaus.jparsec.Terminals.StringLiteral;
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
