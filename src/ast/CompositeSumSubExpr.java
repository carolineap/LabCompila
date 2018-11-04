package ast;

import lexer.Token;

public class CompositeSumSubExpr extends SumSubExpr {
	
	public CompositeSumSubExpr(Expr left, Token op, Expr right) {
	    this.left = left;
	    this.right = right;
	    this.op = op;
	 }
	    
	Expr left;
	Token op;
	Expr right;

}
