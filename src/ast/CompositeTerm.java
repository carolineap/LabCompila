package ast;

import lexer.Token;

public class CompositeTerm extends Term {
	
	 public CompositeTerm(Expr left, Token op, Expr right) {
	    	this.left = left;
	    	this.right = right;
	    	this.op = op;
	  }
	    
	Expr left;
	Token op;
	Expr right;

}