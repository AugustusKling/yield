package yield.config.function.where;

import yield.json.JsonEvent;

public abstract class ExprUnary extends Expr {
	protected Expr expr;
	private String name;

	public ExprUnary(String name, Expr expr) {
		this.name = name;
		this.expr = expr.reduce();
	}

	@Override
	public String toString() {
		return "ExprInvoUn-" + name + "(" + expr + ")";
	}

	protected Object getValue(JsonEvent context) {
		ExprLiteral literal = expr.apply(context);
		Object value = literal.getValue();
		return value;
	}

	@Override
	public boolean isContextDependent() {
		return expr.isContextDependent();
	}
}