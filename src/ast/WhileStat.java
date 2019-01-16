/* 	Caroline Aparecida de Paula Silva 
 	Isabela Sayuri Matsumoto 		*/
 	
package ast;
import java.util.ArrayList;

public class WhileStat extends Statement {
	
	public WhileStat(Expr expr, ArrayList<Statement> statList) {
		this.expr = expr;
		this.statList = statList;
	}
	
	private Expr expr;
	private ArrayList<Statement> statList;

}
