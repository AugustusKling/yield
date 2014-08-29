package yield.config.function.where;

public abstract class ExprBinary extends Expr {
	protected Expr expr1;
	protected Expr expr2;
	private String name;

	public ExprBinary(String name, Expr expr1, Expr expr2) {
		this.name = name;
		this.expr1 = expr1.reduce();
		this.expr2 = expr2.reduce();
	}

	@Override
	public String toString() {
		return "ExprInvoBin-" + name + "(" + expr1 + "," + expr2 + ")";
	}

	@Override
	public boolean isContextDependent() {
		return expr1.isContextDependent() || expr2.isContextDependent();
	}

}