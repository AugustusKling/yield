package yield.config.function.where;

import yield.json.JsonEvent;

public class ExprBinaryLowerThan extends ExprBinary {
	/**
	 * {@code true} in case the result of evaluation is {@code true} for
	 * expressions with equal natural ordering.
	 */
	private boolean equalMatches;

	public ExprBinaryLowerThan(String name, Expr expr1, Expr expr2,
			boolean equalMatches) {
		super(name, expr1, expr2);
		this.equalMatches = equalMatches;
	}

	@Override
	protected ExprLiteral evaluate(JsonEvent context) {
		Object value1 = expr1.apply(context).getValue();
		Object value2 = expr2.apply(context).getValue();
		if (!getClass(value1).equals(getClass(value2))) {
			throw new IllegalArgumentException(
					"Can only compare objects of the same type. "
							+ getClass(value1) + " and " + getClass(value2)
							+ " are thus not usable for comparison.");
		} else if (value1 instanceof Comparable) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			int compareResult = ((Comparable) value1).compareTo(value2);
			return new ExprLiteral(compareResult < 0
					|| (equalMatches && compareResult == 0));
		} else {
			throw new IllegalArgumentException("Objects of type "
					+ getClass(value1) + " are not comparable.");
		}
	}
}
