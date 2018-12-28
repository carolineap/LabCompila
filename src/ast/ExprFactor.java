package ast;

public class ExprFactor extends Factor {

	public ExprFactor(Expr expr) {
		this.expr = expr;
	}
	
	public Type getType() {
		return expr.getType();
	}

	private Expr expr;

	
}
