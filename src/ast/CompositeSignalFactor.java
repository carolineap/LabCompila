/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;

import lexer.Token;

public class CompositeSignalFactor extends SignalFactor {
	
	public CompositeSignalFactor(Token op, Expr right) {
	    	this.right = right;
	    	this.op = op;
	}
	
	@Override
	public Type getType() {
		
		if (op == null) {
			return right.getType();
		}
			
		if (op != null && right.getType() == Type.intType) {
			return right.getType();
		} 
		
		return Type.undefType;
		
	}
	    
	    
	Token op;
	Expr right;

}
