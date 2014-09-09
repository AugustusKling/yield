package yield.json;

import yield.config.function.where.Expr;
import yield.config.function.where.FilterParser;

/**
 * Template to create {@link String} by evaluating context in template
 * expression. {@link String}.
 */
public class Template {
	private Expr template;

	public Template(String template) {
		this.template = new FilterParser().buildExpression(template);
	}

	/**
	 * Fills in placeholders.
	 * 
	 * @param data
	 *            Values for the placeholders.
	 * @return Result from evaluating expression.
	 */
	public String apply(JsonEvent data) {
		return template.apply(data).getValue().toString();
	}
}
