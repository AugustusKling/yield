package yield.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yield.core.ValueMapper;

/**
 * Chops properties and stores fragments as additional properties.
 * 
 * Patterns can be given as Java style regular expression or named grok patterns
 * (for example %{IP:client} to look for an IP address and name it client).
 * Named groups in the pattern will be added as fields to the resulting event.
 *
 * @see "http://logstash.net/docs/1.4.0/filters/grok"
 */
public class JsonGrok implements ValueMapper<JsonEvent, JsonEvent> {
	private Pattern p;
	private String path;
	private Set<String> groupNames;

	/**
	 * General patterns that can be used to construct parsers.
	 *
	 * @see <a
	 *      href="https://github.com/elastic/logstash/tree/v1.4.2/patterns">https://github.com/elastic/logstash/tree/v1.4.2/patterns</a>
	 *      for examples.
	 */
	private static Map<String, String> globalPatterns = new HashMap<>();
	static {
		globalPatterns.put("MAC", "(?:%{CISCOMAC}|%{WINDOWSMAC}|%{COMMONMAC})");
		globalPatterns.put("CISCOMAC",
				"(?:(?:[A-Fa-f0-9]{4}\\.){2}[A-Fa-f0-9]{4})");
		globalPatterns.put("WINDOWSMAC",
				"(?:(?:[A-Fa-f0-9]{2}-){5}[A-Fa-f0-9]{2})");
		globalPatterns.put("COMMONMAC",
				"(?:(?:[A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2})");
		globalPatterns
				.put("IPV6",
						"((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?");
		globalPatterns
				.put("IPV4",
						"(?<![0-9])(?:(?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2})[.](?:25[0-5]|2[0-4][0-9]|[0-1]?[0-9]{1,2}))(?![0-9])");
		globalPatterns.put("IP", "(?:%{IPV6}|%{IPV4})");
		globalPatterns
				.put("HOSTNAME",
						"\\b(?:[0-9A-Za-z][0-9A-Za-z-]{0,62})(?:\\.(?:[0-9A-Za-z][0-9A-Za-z-]{0,62}))*(\\.?|\\b)");
		globalPatterns.put("HOST", "%{HOSTNAME}");
		globalPatterns.put("IPORHOST", "(?:%{HOSTNAME}|%{IP})");
		globalPatterns.put("HOSTPORT", "%{IPORHOST}:%{POSINT}");
	}

	/**
	 * @param path
	 *            Path in the JSON to get the property value.
	 * @param pattern
	 *            Regular expression to match for fragments. Each named group
	 *            results in a new fragment being added.
	 */
	public JsonGrok(String path, String pattern) {
		this.path = path;

		// Convert named grok expressions to regex named groups.
		pattern = pattern.replaceAll("%\\{(\\w+):(\\w+)\\}", "(?<$2>%{$1})");
		// Resolve nested grok expression that are build using the global
		// patterns.
		String unmodifiedPattern;
		do {
			unmodifiedPattern = pattern;
			for (Entry<String, String> grokPattern : globalPatterns.entrySet()) {
				pattern = pattern.replace("%{" + grokPattern.getKey() + "}",
						grokPattern.getValue());
			}
		} while (!unmodifiedPattern.equals(pattern));

		// Get names of named groups.
		Pattern pGroups = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
		Matcher mGroups = pGroups.matcher(pattern);
		groupNames = new TreeSet<>();
		while (mGroups.find()) {
			groupNames.add(mGroups.group(1));
		}

		p = Pattern.compile(pattern, Pattern.DOTALL | Pattern.UNICODE_CASE
				| Pattern.UNICODE_CHARACTER_CLASS);
	}

	@Override
	public JsonEvent map(JsonEvent value) {
		String fieldContent = value.get(path);
		if (fieldContent == null) {
			// Cannot chop field because field is not in JSON.
			throw new IllegalArgumentException("Instructed to match against "
					+ path + " but this property is missing in event " + value);
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
