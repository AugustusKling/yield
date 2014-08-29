package yield.config.function.where;

import yield.json.JsonEvent;

/**
 * Converts objects to {@link String}s and concatenates them yielding a
 * {@link String}.
 */
public class ExprBinaryConcat extends ExprBinary {

	public ExprBinaryConcat(String name, Expr expr1, Expr expr2) {
		super(name, expr1, expr2);
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		ExprLiteral lit1 = expr1.apply(context);
		ExprLiteral lit2 = expr2.apply(context);
		if (lit1.isUnknown() || lit2.isUnknown()) {
			return new ExprLiteral();
		} else {
			return new ExprLiteral(toStringOrEmpty(lit1)
					+ toStringOrEmpty(lit2));
		}
	}

	private String toStringOrEmpty(ExprLiteral lit) {
		if (lit.getValue() == null) {
			return "";
		} else {
			return lit.getValue().toString();
		}
	}

}
