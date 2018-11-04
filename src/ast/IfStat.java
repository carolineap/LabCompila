package ast;
import java.util.ArrayList;

public class IfStat extends Statement {
	
	public IfStat(Expr expr, ArrayList<Statement> ifPart, ArrayList<Statement> elsePart) {
		this.expr = expr;
		this.ifPart = ifPart;
		this.elsePart = elsePart;
	}
	
	private Expr expr;
	private ArrayList<Statement> ifPart;
	private ArrayList<Statement> elsePart;
	

}
