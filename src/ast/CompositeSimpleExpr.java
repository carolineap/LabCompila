package ast;

import lexer.Token;

public class CompositeSimpleExpr extends SimpleExpr {

	 public CompositeSimpleExpr(Expr left, Token op, Expr right) {
	    	this.left = left;
	    	this.right = right;
	    	this.op = op;
	  }
	    
    Expr left;
	Token op;
	Expr right;
	
}
