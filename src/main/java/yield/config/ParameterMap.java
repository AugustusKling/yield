package yield.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.codehaus.jparsec.functors.Pair;

import yield.config.ParameterMap.Param;

public class ParameterMap<Parameter extends Enum<Parameter> & Param> {
	public static interface Param {
		public Object getDefault();
	}

	private Map<Parameter, String> map = new HashMap<>();

	public ParameterMap(final Class<Parameter> parameters,
			List<String> positional, List<Pair<String, String>> named) {
		Parameter[] enumConstants = parameters.getEnumConstants();
		if (positional.size() > enumConstants.length) {
			throw new IllegalArgumentException(
					"More positional parameters provided than defined by the function.");
		}
		// Add positional parameters.
		for (int pos = 0; pos < positional.size(); pos++) {
			map.put(enumConstants[pos], positional.get(pos));
		}
		// Add named parameters.
		for (Pair<String, String> p : named) {
			if (map.containsKey(p.a)) {
				throw new IllegalArgumentException("Parameter " + p.a
						+ " specified twice.");
			} else {
				try {
					map.put(Enum.valueOf(parameters, p.a), p.b);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Unknown parameter "
							+ p.a, e);
				}
			}
		}
	}

	private Object get(Parameter key) {
		if (map.containsKey(key)) {
			return map.get(key);
		} else {
			try {
				return key.getDefault();
			} catch (UnsupportedOperationException e) {
				String docStringOutput;
				try {
					ShortDocumentation shortDocumentation = key
							.getDeclaringClass().getField(key.name())
							.getAnnotation(ShortDocumentation.class);
					if (shortDocumentation == null) {
						// Annotation is not present.
						docStringOutput = "";
					} else {
						String docString = shortDocumentation.text();
						if (docString == null || docString.isEmpty()) {
							docStringOutput = "";
						} else {
							docStringOutput = " (" + docString + ")";
						}
					}
				} catch (NoSuchFieldException | SecurityException e1) {
					docStringOutput = "";
					Logger.getLogger(ParameterMap.class).error(
							"Failed to read documentation annotation.", e1);
				}
				throw new NoSuchElementException("No value defined for " + key
						+ docStringOutput + ".");
			}
		}
	}

	public String getString(Parameter key) {
		return (String) get(key);
	}

	public boolean getBoolean(Parameter key) {
		Object value = get(key);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		} else if ("true".equals(value)) {
			return true;
		} else if ("false".equals(value)) {
			return false;
		} else {
			throw new ClassCastException("Cannot convert value of parameter "
					+ key + " to Boolean. Given value was " + value);
		}
	}

	public int getInteger(Parameter key) {
		Object value = get(key);
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		} else if (value instanceof String) {
			try {
				return Integer.valueOf((String) value);
			} catch (NumberFormatException e) {
				throw new NumberFormatException(
						"Cannot convert value of parameter " + key
								+ " to Integer. Given value was " + value);
			}
		} else {
			throw new ClassCastException("Cannot convert value of parameter "
					+ key + " to Integer. Given value was " + value);
		}
	}

	/**
	 * @see Map#containsKey(Object)
	 */
	public boolean containsKey(Parameter key) {
		return map.containsKey(key);
	}
}
