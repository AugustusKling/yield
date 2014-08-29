package yield.config.function.where;

import yield.json.JsonEvent;

public class ExprUnaryNot extends ExprUnary {

	public ExprUnaryNot(String name, Expr from) {
		super(name, from);
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		Object value = getValue(context);
		if (value instanceof Boolean) {
			return new ExprLiteral(!((Boolean) value).booleanValue());
		} else {
			throw new IllegalArgumentException("Cannot process value of type "
					+ getClass(value));
		}
	}
}
