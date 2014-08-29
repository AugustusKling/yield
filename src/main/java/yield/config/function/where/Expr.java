package yield.config.function.where;

import yield.json.JsonEvent;

/**
 * Base for syntax tree.
 */
public abstract class Expr {
	protected abstract ExprLiteral evaluate(JsonEvent context);

	/**
	 * Evaluates this expression in the given context. The context defines
	 * values for possibly existing placeholders.
	 * 
	 * @param context
	 *            Placeholder/reference values. Parameter is ignored if
	 *            {@link #isContextDependent()} returns {@code false}.
	 * @return Evaluation result.
	 */
	public ExprLiteral apply(JsonEvent context) {
		try {
			return evaluate(context);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Cannot evaluate " + this, e);
		}
	}

	protected Class<?> getClass(Object value) {
		if (value == null) {
			return Void.class;
		} else {
			return value.getClass();
		}
	}

	/**
	 * @return {@code true} if evaluation with another context can influence
	 *         evaluation result.
	 */
	public abstract boolean isContextDependent();

	/**
	 * @return Simpler version of expression.
	 */
	protected Expr reduce() {
		if (isContextDependent()) {
			return this;
		} else {
			return apply(null);
		}
	}
}
