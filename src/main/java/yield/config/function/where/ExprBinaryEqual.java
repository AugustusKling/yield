package yield.config.function.where;

import yield.json.JsonEvent;

public class ExprBinaryEqual extends ExprBinary {

	public ExprBinaryEqual(String name, Expr expr1, Expr expr2) {
		super(name, expr1, expr2);
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		Object value1 = expr1.apply(context).getValue();
		Object value2 = expr2.apply(context).getValue();
		return new ExprLiteral((value1 == value2) || value1.equals(value2));
	}

}
