package ast;

public class AssertStat extends Statement {

	public AssertStat(Expr expr, String string) {
		this.expr = expr;
		this.string = string;
	}
	
	private Expr expr;
	private String string;
	
}
