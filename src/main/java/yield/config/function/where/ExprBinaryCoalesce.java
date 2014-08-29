package yield.config.function.where;

import yield.json.JsonEvent;

/**
 * Evaluates to {@code expr1} with fallback to {@code expr2} if value of
 * {@code expr1} is unknown.
 */
public class ExprBinaryCoalesce extends ExprBinary {

	public ExprBinaryCoalesce(String name, Expr expr1, Expr expr2) {
		super(name, expr1, expr2);
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		ExprLiteral result1 = expr1.apply(context);
		if (result1.isUnknown() || result1.getValue() == null) {
			return expr2.apply(context);
		} else {
			return result1;
		}
	}

}
