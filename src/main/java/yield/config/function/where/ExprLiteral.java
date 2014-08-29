package yield.config.function.where;

import yield.json.JsonEvent;

public class ExprLiteral extends Expr {
	private enum Unknown {
		value;
	}

	private Object value;

	public ExprLiteral(Object value) {
		this.value = value;
	}

	public ExprLiteral() {
		this(Unknown.value);
	}

	@Override
	public String toString() {
		return "ExprLit(" + value + ")";
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		return this;
	}

	public Object getValue() {
		return value;
	}

	/**
	 * @return {@code true} if contained value is not defined.
	 */
	public boolean isUnknown() {
		return value == Unknown.value;
	}
}