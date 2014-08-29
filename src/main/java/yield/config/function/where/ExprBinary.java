package yield.config.function.where;

public abstract class ExprBinary extends Expr {
	protected Expr expr1;
	protected Expr expr2;
	private String name;

	public ExprBinary(String name, Expr expr1, Expr expr2) {
		this.name = name;
		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	@Override
	public String toString() {
		return "ExprInvoBin-" + name + "(" + expr1 + "," + expr2 + ")";
	}
}