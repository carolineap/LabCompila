package ast;

import lexer.Token;

public class CompositeSimpleExpr extends SimpleExpr {

	public CompositeSimpleExpr(Expr left, Token op, Expr right) {
	    	this.left = left;
	    	this.right = right;
	    	this.op = op;
	}
	
	@Override
	public Type getType() {
		
		Type rightType = right.getType();
		Type leftType = left.getType();
		
		if ( (rightType == Type.intType || rightType == Type.stringType) && (leftType == Type.intType || leftType == Type.stringType)) {
			return Type.stringType;
		}
		
		return Type.undefType;
	}
	    

    Expr left;
	Token op;
	Expr right;
	
}
