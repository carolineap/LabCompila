package ast;

import lexer.Token;

public class CompositeTerm extends Term {
	
	public CompositeTerm(Expr left, Token op, Expr right) {
	    	this.left = left;
	    	this.right = right;
	    	this.op = op;
	}
	
	@Override
	public Type getType() {
		
		Type rightType = right.getType();
		Type leftType = left.getType();
		
		if (leftType == Type.intType && rightType == leftType) {
			return leftType;
		}
		
		return Type.undefType;
	}
	    
	    
	Expr left;
	Token op;
	Expr right;

}