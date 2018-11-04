package ast;
import lexer.*;

public class CompositeExpr extends Expr {

	@Override
	public void genC(PW pw, boolean putParenthesis) {
		// TODO Auto-generated method stub

	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}
	    
    public CompositeExpr(Expr left, Token op, Expr right) {
    	this.left = left;
    	this.right = right;
    	this.op = op;
    }
    
    Expr left;
	Token op;
	Expr right;

}
