package ast;

import lexer.Token;

public class CompositeFactor extends Factor {
	
	 public CompositeFactor(Token op, Factor right) {
	    	this.right = right;
	    	this.op = op;
	  }
	    
	Token op;
	Factor right;

}
