package yield.config.function.where;

import yield.json.JsonEvent;

public class ExprUnaryMinus extends ExprUnary {

	public ExprUnaryMinus(String name, Expr from) {
		super(name, from);
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		Object value = getValue(context);
		if (value instanceof Number) {
			return new ExprLiteral(-1 * ((Number) value).doubleValue());
		} else {
			throw new IllegalArgumentException("Cannot process value of type "
					+ getClass(value));
		}
	}
}