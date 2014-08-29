package yield.config.function.where;

import yield.json.JsonEvent;

/**
 * Converts {@link CharSequence}s to lower case.
 */
public class ExprUnaryLower extends ExprUnary {

	public ExprUnaryLower(String name, Expr from) {
		super(name, from);
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		Object value = getValue(context);
		if (value instanceof CharSequence) {
			return new ExprLiteral(value.toString().toLowerCase());
		} else {
			throw new IllegalArgumentException("Cannot form lowercase of "
					+ getClass(value));
		}
	}

}
