package yield.config;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.annotation.Nonnull;

import yield.core.Main;
import yield.core.SourceProvider;
import yield.core.Yielder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a functions in yield's configuration file.
 */
public class FunctionConfig {
	private Class<? extends SourceProvider<?>> providerClass;

	protected FunctionConfig() {
	}

	protected FunctionConfig(Class<? extends SourceProvider<?>> providerClass) {
		this.providerClass = providerClass;
	}

	/**
	 * @return Type of yielded events. For example
	 *         {@code java.util.Map<java.lang.String,java.lang.Object>}
	 */
	protected String getResultEventType() {
		return "java.lang.Object";
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	protected TypedYielder wrapResultingYielder(
			Yielder<? extends Object> yielder) {
		return new TypedYielder(getResultEventType(), (Yielder<Object>) yielder);
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
	@SuppressWarnings("unchecked")
	@Nonnull
	public TypedYielder getSource(String args, Map<String, TypedYielder> context) {
		ObjectNode config = parseArguments(args);
		try {
			Yielder<?> yielder;
			if (providerClass != null) {
				yielder = this.providerClass
						.getConstructor(Main.class, ObjectNode.class)
						.newInstance(null, config).getQueue();
			} else {
				throw new RuntimeException(
						"Override getSource(...) when using the FunctionConfig() constructor.");
			}
			return new TypedYielder(getResultEventType(),
					(Yielder<Object>) yielder);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Failed to construct EventSource.", e);
		}
	}

	/**
	 * Converts arguments to JSON.
	 * 
	 * @param args
	 *            Function argument string. This is the text after the function
	 *            name.
	 * @return Parsed arguments as key-value mapping.
	 */
	protected ObjectNode parseArguments(String args) {
		// TODO Support for escaping.
		String[] parts = args.split("\\s*=\\s*");
		if (parts.length < 2 || parts.length % 2 != 0) {
			throw new RuntimeException("Failed to parse params " + args);
		}
		ObjectNode config = new ObjectMapper().createObjectNode();
		for (int i = 1; i <= parts.length; i = i + 2) {
			String name = parts[i - 1];
			String value = parts[i];
			config.put(name, value);
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
