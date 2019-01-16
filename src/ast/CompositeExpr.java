/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;
import lexer.*;

public class CompositeExpr extends Expr {

	public Type getType() {
		
		Type rightType = right.getType();
		Type leftType = left.getType();
			
		if	(this.op == Token.GT || this.op == Token.GE || this.op == Token.LT || this.op == Token.LE) {
			if (leftType == Type.intType && rightType == leftType) {
				return Type.booleanType;
			}
		} else if (this.op == Token.EQ || this.op == Token.NEQ) {
			
			if (leftType == rightType) {
				return Type.booleanType;
			}
			
			if ((leftType == Type.stringType && rightType == Type.nilType) || (rightType == Type.stringType && leftType == Type.nilType)) {
				return Type.booleanType;
			}
			
			if ((leftType instanceof ClassDec && rightType == Type.nilType) || (rightType instanceof ClassDec && leftType == Type.nilType)) {
				return Type.booleanType;
			}
		}
		
		return Type.undefType;
		
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
