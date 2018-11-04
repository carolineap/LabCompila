package ast;

public class WriteStat extends Statement {
	
	public WriteStat(Expr expr) {
		this.expr = expr;
	}

	private Expr expr;

}
