package yield.config.function.where;

import yield.json.JsonEvent;

public class ExprRef extends Expr {
	private String name;

	public ExprRef(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ExprRef(" + name + ")";
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		if (context.getObject().has(name)) {
			// Value is known which includes known to be null.
			return new ExprLiteral(context.get(name));
		} else {
			// Value is unknown as property is not set in JsonEvent.
			return new ExprLiteral();
		}
	}
}