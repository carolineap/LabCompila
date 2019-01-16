/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;

public class AssignExpr extends Statement {

	public AssignExpr(Expr left, Expr right) {
		this.right = right;
		this.left = left;
	}
	
	private Expr right;
	private Expr left;
	
	
}
