package yield.config.function.where;

import yield.json.JsonEvent;

public class ExprBinaryOr extends ExprBinary {

	public ExprBinaryOr(String name, Expr expr1, Expr expr2) {
		super(name, expr1, expr2);
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		return new ExprLiteral(isTrue(expr1, context) || isTrue(expr2, context));
	}

	private boolean isTrue(Expr expr, JsonEvent context) {
		Object value = expr.apply(context).getValue();
		if (value instanceof Boolean) {
			return Boolean.TRUE.equals(value);
		} else {
			throw new IllegalArgumentException("Cannot use value of type "
					+ getClass(value) + " as Boolean.");
		}
	}

	@Override
	public boolean isContextDependent() {
		if (expr1.isContextDependent() == false && isTrue(expr1, null)) {
			return false;
		} else if (expr2.isContextDependent() == false && isTrue(expr2, null)) {
			return false;
		}
		return true;
	}
}
