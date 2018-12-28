package ast;

import lexer.Token;

public class CompositeFactor extends Factor {
	
	public CompositeFactor(Token op, Factor right) {
	    	this.right = right;
	    	this.op = op;
	}
	 
	public Type getType() {
		
		if (right.getType() == Type.booleanType) {
			return Type.booleanType;
		}
		
		return Type.undefType;
	}

	Token op;
	Factor right;

}
