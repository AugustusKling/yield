package yield.json;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yield.core.ValueMapper;

/**
 * Chops properties and stores fragments as additional properties.
 * 
 * @see http://logstash.net/docs/1.4.0/filters/grok
 */
public class JsonGrok implements ValueMapper<JsonEvent, JsonEvent> {
	private Pattern p;
	private String path;
	private Set<String> groupNames;

	/**
	 * @param path
	 *            Path in the JSON to get the property value.
	 * @param pattern
	 *            Regular expression to match for fragments. Each named group
	 *            results in a new fragment being added.
	 */
	public JsonGrok(String path, String pattern) {
		this.path = path;

		// Get names of named groups.
		Pattern pGroups = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
		Matcher mGroups = pGroups.matcher(pattern);
		groupNames = new TreeSet<>();
		while (mGroups.find()) {
			groupNames.add(mGroups.group(1));
		}

		p = Pattern.compile(pattern, Pattern.DOTALL);
	}

	@Override
	public JsonEvent map(JsonEvent value) {
		String fieldContent = value.get(path);
		if (fieldContent == null) {
			// Cannot chop field because field is not in JSON.
			System.out.println("Unparsable: " + fieldContent);
			return value;
		} else {
			Matcher m = p.matcher(fieldContent);
			if (m.find()) {
				// Create new object to hold additional fragments.
				JsonEvent result = new JsonEvent(value);
				for (String name : groupNames) {
					// Save fragments, possibly overwriting the existing
					// properties.
					result.put(name, m.group(name));
				}
				return result;
			} else {
				// Return input since no modification occurred.
				return value;
			}
		}
	}
}
