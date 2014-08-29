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
		if (expr1.apply(context).isUnknown()) {
			return expr2.apply(context);
		} else {
			return expr1.apply(context);
		}
	}

}
