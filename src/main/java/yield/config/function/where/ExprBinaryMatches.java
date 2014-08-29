package yield.config.function.where;

import java.util.regex.Pattern;

import yield.json.JsonEvent;

/**
 * Applies regular expression. Yield {@code true} if pattern matches the whole
 * {@code value}.
 */
public class ExprBinaryMatches extends ExprBinary {

	private String value = null;
	private Pattern pattern = null;

	public ExprBinaryMatches(String name, Expr value, Expr pattern) {
		super(name, value, pattern);
		if (value.isContextDependent() == false) {
			this.value = getEvaluatedValue(value, null);
		}
		if (pattern.isContextDependent() == false) {
			this.pattern = getEvaluatedPattern(pattern, null);
		}
	}

	private Pattern getEvaluatedPattern(Expr pattern, JsonEvent context) {
		Object evaluatedPattern = pattern.apply(context).getValue();
		if (!(evaluatedPattern instanceof String)) {
			throw new IllegalArgumentException(
					"Regular expression for matching must be given as String but is "
							+ getClass(evaluatedPattern));
		} else {
			return Pattern.compile((String) evaluatedPattern, Pattern.DOTALL
					| Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
		}
	}

	private String getEvaluatedValue(Expr value, JsonEvent context) {
		Object evaluatedValue = value.apply(context).getValue();
		if (!(evaluatedValue instanceof String)) {
			throw new IllegalArgumentException(
					"Value to match against pattern must evaluate to String but is "
							+ getClass(evaluatedValue));
		} else {
			return (String) evaluatedValue;
		}
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		String value;
		if (this.expr1.isContextDependent()) {
			value = getEvaluatedValue(expr1, context);
		} else {
			value = this.value;
		}
		Pattern pattern;
		if (this.expr2.isContextDependent()) {
			pattern = getEvaluatedPattern(expr2, context);
		} else {
			pattern = this.pattern;
		}
		return new ExprLiteral(pattern.matcher(value).matches());
	}

}
