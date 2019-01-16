/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;

public class ReturnStat extends Statement {
	
	public ReturnStat(Expr expr) {
		this.expr = expr;
	}

	private Expr expr;
	
}
