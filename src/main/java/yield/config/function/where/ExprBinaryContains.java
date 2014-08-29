package yield.config.function.where;

import java.util.Collection;

import yield.json.JsonEvent;

public class ExprBinaryContains extends ExprBinary {

	public ExprBinaryContains(String name, Expr expr1, Expr expr2) {
		super(name, expr1, expr2);
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		Object whole = expr1.apply(context).getValue();
		Object part = expr2.apply(context).getValue();
		if (whole instanceof CharSequence) {
			if (part instanceof CharSequence) {
				return new ExprLiteral(whole.toString().contains(
						part.toString()));
			} else {
				throw new IllegalArgumentException("Types of "
						+ getClass(whole) + " and " + getClass(part)
						+ " cannot be within each other.");
			}
		} else if (whole instanceof Collection) {
			return new ExprLiteral(((Collection<?>) whole).contains(part));
		} else {
			throw new IllegalArgumentException("Type " + getClass(whole)
					+ " cannot be handled as Collection.");
		}
	}

}
