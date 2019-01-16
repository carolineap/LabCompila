/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;

import lexer.Token;

public class CompositeSumSubExpr extends SumSubExpr {
	
	public CompositeSumSubExpr(Expr left, Token op, Expr right) {
	    this.left = left;
	    this.right = right;
	    this.op = op;
	}
	
	@Override
	public Type getType() {
		
		Type rightType = right.getType();
		Type leftType = left.getType();
		
		if ((op == Token.PLUS || op == Token.MINUS) && leftType == Type.intType && rightType == leftType) {
			return leftType;
		}
		
		if (op == Token.OR && left.getType() == Type.booleanType && right.getType() == left.getType()) {
			return Type.booleanType;
		}
		
		return Type.undefType;
	}
	    
	    
	Expr left;
	Token op;
	Expr right;

}
