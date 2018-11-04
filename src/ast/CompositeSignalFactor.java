package ast;

import lexer.Token;

public class CompositeSignalFactor extends SignalFactor {
	
	 public CompositeSignalFactor(Token op, Expr right) {
	    	this.right = right;
	    	this.op = op;
	  }
	    
	Token op;
	Expr right;

}
