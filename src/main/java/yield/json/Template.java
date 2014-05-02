package yield.json;

import java.util.Map.Entry;

/**
 * Template to create {@link String} by replacing placeholders in template
 * {@link String}.
 */
public class Template {
	private String template;

	public Template(String template) {
		this.template = template;
	}

	/**
	 * Fills in placeholders (for example <code>${the_name}</code>).
	 * 
	 * @param data
	 *            Values for the placeholders.
	 * @return Result from filling in placeholders in template.
	 */
	public String apply(JsonEvent data) {
		String result = template;
		for (Entry<String, String> entry : data) {
			result = result.replace("${" + entry.getKey() + "}",
					entry.getValue());
		}
		return result;
	}
}
